package com.virtual_paddock.backend.infrastructure.helper.simulator;

import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkItemRequest;
import com.virtual_paddock.backend.domain.entities.Driver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Order(30)
public class IracingCsvParser implements ISimulatorLogParser {

    @Override
    public boolean supports(String filename, String content) {
        String lowerName = filename != null ? filename.toLowerCase() : "";
        if (lowerName.endsWith(".csv")) return true;
        // Si no es json ni xml, el default es csv/tabular
        String trimmed = content != null ? content.trim() : "";
        return !trimmed.startsWith("{") && !trimmed.startsWith("[") && !trimmed.startsWith("<");
    }

    @Override
    public List<RaceResultBulkItemRequest> parse(String content, List<Driver> allDrivers) throws Exception {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return items;

            String delimiter = headerLine.contains(";") ? ";" : ",";
            String[] headers = Arrays.stream(headerLine.split(delimiter))
                    .map(h -> h.replace("\"", "").trim().toLowerCase())
                    .toArray(String[]::new);

            int posIdx = findIndex(headers, "fin pos", "pos", "position", "p");
            int nameIdx = findIndex(headers, "driver", "name", "pilot", "driver name");
            int carNumIdx = findIndex(headers, "car #", "car num", "car_number", "number", "num", "dorsal");
            int totalTimeIdx = findIndex(headers, "interval", "total time", "time", "gap", "tiempo");
            int bestLapIdx = findIndex(headers, "fastest lap", "best lap", "fastest lap time", "best lap time", "best");

            String line;
            int order = 1;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(delimiter);

                String driverName = nameIdx >= 0 && nameIdx < values.length ? cleanValue(values[nameIdx]) : null;
                String carNumber = carNumIdx >= 0 && carNumIdx < values.length ? cleanValue(values[carNumIdx]) : null;
                String totalTime = totalTimeIdx >= 0 && totalTimeIdx < values.length ? cleanValue(values[totalTimeIdx]) : null;
                String bestLap = bestLapIdx >= 0 && bestLapIdx < values.length ? cleanValue(values[bestLapIdx]) : null;
                Integer finishPos = posIdx >= 0 && posIdx < values.length ? tryParseInt(cleanValue(values[posIdx])) : order;

                Driver matched = DriverMatchingHelper.matchDriver(driverName, null, carNumber, allDrivers);
                UUID resolvedDriverId = matched != null ? matched.getId() : null;
                String resolvedName = matched != null ? matched.getName() : driverName;
                String resolvedCarNumber = carNumber != null ? carNumber : (matched != null ? matched.getCarNumber() : "");

                items.add(RaceResultBulkItemRequest.builder()
                        .driverId(resolvedDriverId)
                        .driverName(resolvedName)
                        .carNumber(resolvedCarNumber)
                        .finishOrder(finishPos != null ? finishPos : order)
                        .totalTime(totalTime)
                        .bestLapTime(bestLap)
                        .penaltiesSeconds(0)
                        .fastestLap(false)
                        .build());
                order++;
            }
        }
        return items;
    }

    private String cleanValue(String val) {
        return val != null ? val.replace("\"", "").trim() : "";
    }

    private Integer tryParseInt(String s) {
        if (s == null) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private int findIndex(String[] headers, String... candidates) {
        for (int i = 0; i < headers.length; i++) {
            for (String c : candidates) {
                if (headers[i].contains(c)) return i;
            }
        }
        return -1;
    }
}
