package com.virtual_paddock.backend.infrastructure.helper.simulator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkItemRequest;
import com.virtual_paddock.backend.domain.entities.Driver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(10)
public class AssettoCorsaJsonParser implements ISimulatorLogParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String filename, String content) {
        String lowerName = filename != null ? filename.toLowerCase() : "";
        String trimmed = content != null ? content.trim() : "";
        return lowerName.endsWith(".json") || trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    @Override
    public List<RaceResultBulkItemRequest> parse(String content, List<Driver> allDrivers) throws Exception {
        JsonNode root = objectMapper.readTree(content);

        if (root.has("Result") || root.has("result")) {
            return parseAssettoCorsa1Json(root, allDrivers);
        } else if (root.has("sessionResult") && root.get("sessionResult").has("leaderBoardLines")) {
            return parseAccJson(root, allDrivers);
        } else if (root.has("results") || root.has("Results") || root.isArray()) {
            return parseGenericJson(root, allDrivers);
        }

        return Collections.emptyList();
    }

    private List<RaceResultBulkItemRequest> parseAssettoCorsa1Json(JsonNode root, List<Driver> allDrivers) {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();
        JsonNode resultNode = root.has("Result") ? root.get("Result") : root.get("result");
        int raceLaps = root.has("RaceLaps") ? root.get("RaceLaps").asInt() : 0;

        Map<String, Integer> driverLapsCount = countAssettoCorsaLaps(root);
        Set<String> seenDrivers = new HashSet<>();
        int order = 1;

        for (int i = 0; i < resultNode.size(); i++) {
            JsonNode res = resultNode.get(i);
            String driverName = res.has("DriverName") ? res.get("DriverName").asText().trim() : "";
            String driverGuid = res.has("DriverGuid") ? res.get("DriverGuid").asText().trim() : "";
            int carId = res.has("CarId") ? res.get("CarId").asInt() : -1;
            long totalTimeMs = res.has("TotalTime") ? res.get("TotalTime").asLong() : 0L;
            long bestLapMs = res.has("BestLap") ? res.get("BestLap").asLong() : 0L;

            if (driverName.isEmpty() && driverGuid.isEmpty() && totalTimeMs <= 0) {
                continue;
            }

            String driverKey = (!driverGuid.isEmpty() ? driverGuid : driverName).toLowerCase();
            if (totalTimeMs <= 0 && seenDrivers.contains(driverKey)) {
                continue;
            }
            if (!driverKey.isEmpty() && totalTimeMs > 0) {
                seenDrivers.add(driverKey);
            }

            int lapsCompleted = driverLapsCount.getOrDefault(driverKey, 0);
            boolean finishedNormally = (raceLaps > 0 && lapsCompleted >= raceLaps) || (raceLaps == 0 && totalTimeMs > 0);
            String status = finishedNormally ? "FINISHED" : "DNF";
            String formattedTotalTime = (finishedNormally && totalTimeMs > 0) ? LapTimeHelper.formatMillis(totalTimeMs) : null;

            Driver matchedDriver = DriverMatchingHelper.matchDriver(driverName, driverGuid, carId >= 0 ? String.valueOf(carId) : null, allDrivers);
            UUID resolvedDriverId = matchedDriver != null ? matchedDriver.getId() : null;
            String resolvedName = matchedDriver != null ? matchedDriver.getName() : (!driverName.isEmpty() ? driverName : "Piloto #" + (carId >= 0 ? carId : order));
            String resolvedCarNumber = carId >= 0 ? String.valueOf(carId) : (matchedDriver != null ? matchedDriver.getCarNumber() : "");

            items.add(RaceResultBulkItemRequest.builder()
                    .driverId(resolvedDriverId)
                    .driverName(resolvedName)
                    .carNumber(resolvedCarNumber)
                    .category(null)
                    .finishOrder(order)
                    .totalTime(formattedTotalTime)
                    .bestLapTime((bestLapMs > 0 && bestLapMs < 99999999L) ? LapTimeHelper.formatMillis(bestLapMs) : null)
                    .lapsCompleted(lapsCompleted)
                    .penaltiesSeconds(0)
                    .fastestLap(false)
                    .polePosition(order == 1)
                    .status(status)
                    .build());

            order++;
        }

        return items;
    }

    private Map<String, Integer> countAssettoCorsaLaps(JsonNode root) {
        Map<String, Integer> lapsCount = new HashMap<>();
        if (root.has("Laps") && root.get("Laps").isArray()) {
            for (JsonNode lap : root.get("Laps")) {
                String name = lap.has("DriverName") ? lap.get("DriverName").asText().trim() : "";
                String guid = lap.has("DriverGuid") ? lap.get("DriverGuid").asText().trim() : "";
                String key = (!guid.isEmpty() ? guid : name).toLowerCase();
                if (!key.isEmpty()) {
                    lapsCount.put(key, lapsCount.getOrDefault(key, 0) + 1);
                }
            }
        }
        return lapsCount;
    }

    private List<RaceResultBulkItemRequest> parseAccJson(JsonNode root, List<Driver> allDrivers) {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();
        JsonNode lines = root.get("sessionResult").get("leaderBoardLines");

        for (int i = 0; i < lines.size(); i++) {
            JsonNode line = lines.get(i);
            JsonNode car = line.get("car");
            JsonNode currentDriver = (car != null && car.has("drivers") && !car.get("drivers").isEmpty())
                    ? car.get("drivers").get(0)
                    : null;

            String firstName = currentDriver != null && currentDriver.has("firstName") ? currentDriver.get("firstName").asText() : "";
            String lastName = currentDriver != null && currentDriver.has("lastName") ? currentDriver.get("lastName").asText() : "";
            String fullName = (firstName + " " + lastName).trim();
            String playerId = currentDriver != null && currentDriver.has("playerId") ? currentDriver.get("playerId").asText() : "";
            String carNumber = car != null && car.has("raceNumber") ? String.valueOf(car.get("raceNumber").asInt()) : null;

            JsonNode timing = line.get("timing");
            Long totalTimeMs = timing != null && timing.has("totalTime") ? timing.get("totalTime").asLong() : null;
            Long bestLapMs = timing != null && timing.has("bestLap") ? timing.get("bestLap").asLong() : null;
            int lapCount = timing != null && timing.has("lapCount") ? timing.get("lapCount").asInt() : 0;

            String status = (totalTimeMs != null && totalTimeMs > 0) ? "FINISHED" : "DNF";

            Driver matchedDriver = DriverMatchingHelper.matchDriver(fullName, playerId, carNumber, allDrivers);
            UUID resolvedDriverId = matchedDriver != null ? matchedDriver.getId() : null;
            String resolvedName = matchedDriver != null ? matchedDriver.getName() : fullName;
            String resolvedCarNumber = carNumber != null ? carNumber : (matchedDriver != null ? matchedDriver.getCarNumber() : "");

            items.add(RaceResultBulkItemRequest.builder()
                    .driverId(resolvedDriverId)
                    .driverName(resolvedName)
                    .carNumber(resolvedCarNumber)
                    .finishOrder(i + 1)
                    .totalTime(LapTimeHelper.formatMillis(totalTimeMs))
                    .bestLapTime(LapTimeHelper.formatMillis(bestLapMs))
                    .lapsCompleted(lapCount > 0 ? lapCount : null)
                    .penaltiesSeconds(0)
                    .fastestLap(false)
                    .polePosition(i == 0)
                    .status(status)
                    .build());
        }
        return items;
    }

    private List<RaceResultBulkItemRequest> parseGenericJson(JsonNode root, List<Driver> allDrivers) {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();
        JsonNode resultsNode = root.has("results") ? root.get("results") : (root.has("Results") ? root.get("Results") : root);

        for (int i = 0; i < resultsNode.size(); i++) {
            JsonNode res = resultsNode.get(i);
            UUID driverId = null;
            if (res.has("driverId") && !res.get("driverId").isNull()) {
                try {
                    driverId = UUID.fromString(res.get("driverId").asText());
                } catch (Exception ignored) {}
            }
            String driverName = res.has("driverName") ? res.get("driverName").asText() : null;
            String carNumber = res.has("carNumber") ? res.get("carNumber").asText() : null;
            String status = res.has("status") ? res.get("status").asText() : "FINISHED";
            Integer laps = res.has("lapsCompleted") ? res.get("lapsCompleted").asInt() : null;

            UUID finalDriverId = driverId;
            Driver matchedDriver = finalDriverId != null
                    ? allDrivers.stream().filter(d -> d.getId().equals(finalDriverId)).findFirst().orElse(null)
                    : DriverMatchingHelper.matchDriver(driverName, null, carNumber, allDrivers);

            UUID resolvedDriverId = matchedDriver != null ? matchedDriver.getId() : driverId;
            String resolvedName = matchedDriver != null ? matchedDriver.getName() : driverName;
            String resolvedCarNumber = carNumber != null ? carNumber : (matchedDriver != null ? matchedDriver.getCarNumber() : "");

            items.add(RaceResultBulkItemRequest.builder()
                    .driverId(resolvedDriverId)
                    .driverName(resolvedName)
                    .carNumber(resolvedCarNumber)
                    .category(res.has("category") ? res.get("category").asText() : null)
                    .totalTime(res.has("totalTime") ? res.get("totalTime").asText() : null)
                    .bestLapTime(res.has("bestLapTime") ? res.get("bestLapTime").asText() : null)
                    .lapsCompleted(laps)
                    .penaltiesSeconds(res.has("penaltiesSeconds") ? res.get("penaltiesSeconds").asInt() : 0)
                    .fastestLap(res.has("fastestLap") && res.get("fastestLap").asBoolean())
                    .polePosition(res.has("polePosition") && res.get("polePosition").asBoolean())
                    .status(status)
                    .build());
        }
        return items;
    }
}
