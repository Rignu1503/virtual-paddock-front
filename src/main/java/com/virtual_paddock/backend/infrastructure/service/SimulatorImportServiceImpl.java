package com.virtual_paddock.backend.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkItemRequest;
import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkRequest;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISimulatorImportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SimulatorImportServiceImpl implements ISimulatorImportService {

    private final RaceEventRepository raceEventRepository;
    private final DriverRepository driverRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RaceResultBulkRequest parseSimulatorFile(MultipartFile file, Long raceEventId) {
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId)
                .orElseThrow(() -> new EntityNotFoundException("Evento de carrera no encontrado con ID: " + raceEventId));

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<Driver> allDrivers = driverRepository.findAll();

        List<RaceResultBulkItemRequest> items;

        try {
            byte[] fileBytes = file.getBytes();
            String content = decodeUniversalString(fileBytes).trim();

            if (filename.endsWith(".json") || content.startsWith("{") || content.startsWith("[")) {
                items = parseJson(content, allDrivers);
            } else if (filename.endsWith(".xml") || content.startsWith("<")) {
                items = parseXml(content, allDrivers);
            } else {
                items = parseCsv(content, allDrivers);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al procesar el archivo de resultados: " + e.getMessage(), e);
        }

        // Auto-detectar quién tuvo la mejor vuelta general de los items parseados
        resolveFastestLap(items);

        return RaceResultBulkRequest.builder()
                .results(items)
                .build();
    }

    /**
     * Decodifica un array de bytes de forma universal tolerando UTF-8, Windows-1252 e ISO-8859-1.
     */
    private String decodeUniversalString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        // 1. Intentar decodificación UTF-8 estricta
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (Exception ignored) {
            // 2. Si contiene bytes de codificaciones tradicionales (ej. Windows-1252 / ISO-8859-1 en rFactor)
            try {
                return new String(bytes, Charset.forName("Windows-1252"));
            } catch (Exception fallbackIgnored) {
                return new String(bytes, StandardCharsets.ISO_8859_1);
            }
        }
    }

    /**
     * Parsea archivos JSON detectando automáticamente el simulador (AC, ACC o genérico).
     */
    private List<RaceResultBulkItemRequest> parseJson(String content, List<Driver> allDrivers) throws Exception {
        JsonNode root = objectMapper.readTree(content);

        // 1. Formato Assetto Corsa 1 (AC Server JSON con "Result" o "result")
        if (root.has("Result") || root.has("result")) {
            return parseAssettoCorsa1Json(root, allDrivers);
        }
        // 2. Formato ACC (Assetto Corsa Competizione)
        else if (root.has("sessionResult") && root.get("sessionResult").has("leaderBoardLines")) {
            return parseAccJson(root, allDrivers);
        }
        // 3. Formato genérico de Virtual Paddock o array directo
        else if (root.has("results") || root.has("Results") || root.isArray()) {
            return parseGenericJson(root, allDrivers);
        }

        return new ArrayList<>();
    }

    /**
     * Helper modular para parsear resultados del servidor dedicado de Assetto Corsa 1.
     */
    private List<RaceResultBulkItemRequest> parseAssettoCorsa1Json(JsonNode root, List<Driver> allDrivers) {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();
        JsonNode resultNode = root.has("Result") ? root.get("Result") : root.get("result");
        int raceLaps = root.has("RaceLaps") ? root.get("RaceLaps").asInt() : 0;

        // Contar vueltas completadas por cada piloto en el log
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

            // Ignorar slots de autos vacíos en el servidor de AC
            if (driverName.isEmpty() && driverGuid.isEmpty() && totalTimeMs <= 0) {
                continue;
            }

            // Evitar slots duplicados de reconexión
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
            String formattedTotalTime = (finishedNormally && totalTimeMs > 0) ? formatMillis(totalTimeMs) : null;

            Driver matchedDriver = matchDriver(driverName, driverGuid, carId >= 0 ? String.valueOf(carId) : null, allDrivers);
            Long resolvedDriverId = matchedDriver != null ? matchedDriver.getId() : -(long) order;
            String resolvedName = matchedDriver != null ? matchedDriver.getName() : (!driverName.isEmpty() ? driverName : "Piloto #" + (carId >= 0 ? carId : order));
            String resolvedCarNumber = carId >= 0 ? String.valueOf(carId) : (matchedDriver != null ? matchedDriver.getCarNumber() : "");

            items.add(RaceResultBulkItemRequest.builder()
                    .driverId(resolvedDriverId)
                    .driverName(resolvedName)
                    .carNumber(resolvedCarNumber)
                    .category(null) // Usar categoría del campeonato para no fragmentar en categorías de 1 auto
                    .finishOrder(order)
                    .totalTime(formattedTotalTime)
                    .bestLapTime((bestLapMs > 0 && bestLapMs < 99999999L) ? formatMillis(bestLapMs) : null)
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

    /**
     * Helper para contar vueltas por piloto en el array Laps de Assetto Corsa.
     */
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

    /**
     * Helper modular para parsear resultados de Assetto Corsa Competizione (ACC).
     */
    private List<RaceResultBulkItemRequest> parseAccJson(JsonNode root, List<Driver> allDrivers) {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();
        JsonNode lines = root.get("sessionResult").get("leaderBoardLines");

        for (int i = 0; i < lines.size(); i++) {
            JsonNode line = lines.get(i);
            JsonNode car = line.get("car");
            JsonNode currentDriver = (car != null && car.has("drivers") && car.get("drivers").size() > 0)
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

            Driver matchedDriver = matchDriver(fullName, playerId, carNumber, allDrivers);
            Long resolvedDriverId = matchedDriver != null ? matchedDriver.getId() : -(long) (i + 1);
            String resolvedName = matchedDriver != null ? matchedDriver.getName() : fullName;
            String resolvedCarNumber = carNumber != null ? carNumber : (matchedDriver != null ? matchedDriver.getCarNumber() : "");

            items.add(RaceResultBulkItemRequest.builder()
                    .driverId(resolvedDriverId)
                    .driverName(resolvedName)
                    .carNumber(resolvedCarNumber)
                    .finishOrder(i + 1)
                    .totalTime(formatMillis(totalTimeMs))
                    .bestLapTime(formatMillis(bestLapMs))
                    .lapsCompleted(lapCount > 0 ? lapCount : null)
                    .penaltiesSeconds(0)
                    .fastestLap(false)
                    .polePosition(i == 0)
                    .status(status)
                    .build());
        }
        return items;
    }

    /**
     * Helper modular para parsear formato JSON genérico.
     */
    private List<RaceResultBulkItemRequest> parseGenericJson(JsonNode root, List<Driver> allDrivers) {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();
        JsonNode resultsNode = root.has("results") ? root.get("results") : (root.has("Results") ? root.get("Results") : root);

        for (int i = 0; i < resultsNode.size(); i++) {
            JsonNode res = resultsNode.get(i);
            Long driverId = res.has("driverId") ? res.get("driverId").asLong() : null;
            String driverName = res.has("driverName") ? res.get("driverName").asText() : null;
            String carNumber = res.has("carNumber") ? res.get("carNumber").asText() : null;
            String status = res.has("status") ? res.get("status").asText() : "FINISHED";
            Integer laps = res.has("lapsCompleted") ? res.get("lapsCompleted").asInt() : null;

            Driver matchedDriver = driverId != null
                    ? allDrivers.stream().filter(d -> d.getId().equals(driverId)).findFirst().orElse(null)
                    : matchDriver(driverName, null, carNumber, allDrivers);

            Long resolvedDriverId = matchedDriver != null ? matchedDriver.getId() : (driverId != null ? driverId : -(long) (i + 1));
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

    /**
     * Parsea archivos CSV de iRacing o exports tabulares.
     */
    private List<RaceResultBulkItemRequest> parseCsv(String content, List<Driver> allDrivers) throws Exception {
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

                Driver matched = matchDriver(driverName, null, carNumber, allDrivers);
                Long resolvedDriverId = matched != null ? matched.getId() : -(long) order;
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

    /**
     * Parsea archivos XML de rFactor 1, rFactor 2, Automobilista 1 y 2 de forma robusta.
     */
    private List<RaceResultBulkItemRequest> parseXml(String content, List<Driver> allDrivers) throws Exception {
        List<RaceResultBulkItemRequest> items = new ArrayList<>();

        // Limpiar caracteres de control no válidos en XML 1.0 (evita que el parser falle con bytes raros)
        String sanitizedXml = content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        dbFactory.setNamespaceAware(false);
        try {
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception ignored) {}

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        // Usar StringReader a través de InputSource para que el parser use los caracteres decodificados directamente
        InputSource inputSource = new InputSource(new StringReader(sanitizedXml));
        Document doc = dBuilder.parse(inputSource);
        doc.getDocumentElement().normalize();

        // Buscar nodos de Driver prioritariamente dentro de <Race> si existe sesión de carrera
        NodeList driverNodes = null;
        NodeList raceNodes = doc.getElementsByTagName("Race");
        if (raceNodes.getLength() > 0) {
            Element raceEl = (Element) raceNodes.item(0);
            driverNodes = raceEl.getElementsByTagName("Driver");
        }
        if (driverNodes == null || driverNodes.getLength() == 0) {
            driverNodes = doc.getElementsByTagName("Driver");
        }

        for (int i = 0; i < driverNodes.getLength(); i++) {
            Element driverEl = (Element) driverNodes.item(i);

            String name = getTagValue(driverEl, "Name");
            String carNumber = getTagValue(driverEl, "CarNumber");
            String positionStr = getTagValue(driverEl, "Position");
            if (positionStr == null) {
                positionStr = getTagValue(driverEl, "ClassPosition");
            }
            Integer finishPos = tryParseInt(positionStr);

            // rFactor utiliza <FinishTime> para el tiempo final en segundos (ej: 1849.4352) o <TotalTime>
            String totalTimeRaw = getTagValue(driverEl, "FinishTime");
            if (totalTimeRaw == null) {
                totalTimeRaw = getTagValue(driverEl, "TotalTime");
            }
            String totalTime = formatSecondsStringToLapTime(totalTimeRaw);

            // rFactor almacena <BestLapTime> en segundos (ej: 105.3559)
            String bestLapRaw = getTagValue(driverEl, "BestLapTime");
            if (bestLapRaw == null) {
                bestLapRaw = getTagValue(driverEl, "BestLap");
            }
            String bestLap = formatSecondsStringToLapTime(bestLapRaw);

            // Categoría
            String category = getTagValue(driverEl, "CarClass");
            if (category == null) {
                category = getTagValue(driverEl, "CarType");
            }

            // Grid / Pole position
            String gridPosStr = getTagValue(driverEl, "GridPos");
            boolean isPole = "1".equals(gridPosStr);

            // Vueltas completadas
            String lapsStr = getTagValue(driverEl, "Laps");
            Integer laps = tryParseInt(lapsStr);

            // Estado de finalización (FINISHED, DNF, DQ, DNS)
            String finishStatusRaw = getTagValue(driverEl, "FinishStatus");
            String dnfReason = getTagValue(driverEl, "DNFReason");
            String driverStatus = "FINISHED";

            if (finishStatusRaw != null && !finishStatusRaw.trim().isEmpty()) {
                String upper = finishStatusRaw.trim().toUpperCase();
                if (upper.contains("DQ") || upper.contains("DISQUALIFIED")) {
                    driverStatus = "DQ";
                } else if (upper.contains("DNF") || dnfReason != null) {
                    driverStatus = "DNF";
                } else if (upper.contains("DNS")) {
                    driverStatus = "DNS";
                }
            } else if (dnfReason != null && !dnfReason.trim().isEmpty()) {
                driverStatus = "DNF";
            }

            Driver matched = matchDriver(name, null, carNumber, allDrivers);
            Long resolvedDriverId = matched != null ? matched.getId() : -(long) (i + 1);
            String resolvedName = matched != null ? matched.getName() : name;
            String resolvedCarNumber = carNumber != null ? carNumber : (matched != null ? matched.getCarNumber() : "");

            items.add(RaceResultBulkItemRequest.builder()
                    .driverId(resolvedDriverId)
                    .driverName(resolvedName)
                    .carNumber(resolvedCarNumber)
                    .category(category)
                    .finishOrder(finishPos != null ? finishPos : (i + 1))
                    .totalTime(totalTime)
                    .bestLapTime(bestLap)
                    .lapsCompleted(laps)
                    .penaltiesSeconds(0)
                    .fastestLap(false)
                    .polePosition(isPole)
                    .status(driverStatus)
                    .build());
        }
        return items;
    }

    /**
     * Empareja un piloto del log con la base de datos por Gamertag, Nombre o Dorsal con normalización.
     */
    private Driver matchDriver(String name, String gamertag, String carNumber, List<Driver> allDrivers) {
        if (allDrivers == null || allDrivers.isEmpty()) return null;

        // 1. Coincidencia por Gamertag explícito
        if (gamertag != null && !gamertag.trim().isEmpty()) {
            String normTag = normalizeString(gamertag);
            Optional<Driver> byTag = allDrivers.stream()
                    .filter(d -> d.getGamertag() != null && normalizeString(d.getGamertag()).equals(normTag))
                    .findFirst();
            if (byTag.isPresent()) return byTag.get();
        }

        // 2. Coincidencia por Nombre/Nick del log del simulador contra el GAMERTAG registrado en la BD
        if (name != null && !name.trim().isEmpty()) {
            String normName = normalizeString(name);
            Optional<Driver> byGamertag = allDrivers.stream()
                    .filter(d -> {
                        if (d.getGamertag() == null || d.getGamertag().trim().isEmpty()) return false;
                        String dbTag = normalizeString(d.getGamertag());
                        return dbTag.equals(normName) || dbTag.contains(normName) || normName.contains(dbTag);
                    })
                    .findFirst();
            if (byGamertag.isPresent()) return byGamertag.get();
        }

        // 3. Coincidencia por Nombre registrado en la BD (normalizado, sin acentos ni guiones bajos)
        if (name != null && !name.trim().isEmpty()) {
            String normName = normalizeString(name);
            Optional<Driver> byName = allDrivers.stream()
                    .filter(d -> {
                        if (d.getName() == null) return false;
                        String dbNorm = normalizeString(d.getName());
                        return dbNorm.equals(normName)
                                || dbNorm.contains(normName)
                                || normName.contains(dbNorm);
                    })
                    .findFirst();
            if (byName.isPresent()) return byName.get();
        }

        // 4. Coincidencia por Dorsal (#)
        if (carNumber != null && !carNumber.trim().isEmpty()) {
            String cleanNum = carNumber.replaceAll("[^0-9]", "");
            if (!cleanNum.isEmpty()) {
                Optional<Driver> byNum = allDrivers.stream()
                        .filter(d -> {
                            if (d.getCarNumber() == null) return false;
                            String dbClean = d.getCarNumber().replaceAll("[^0-9]", "");
                            return !dbClean.isEmpty() && (cleanNum.equals(dbClean) || (tryParseInt(cleanNum) != null && Objects.equals(tryParseInt(cleanNum), tryParseInt(dbClean))));
                        })
                        .findFirst();
                if (byNum.isPresent()) return byNum.get();
            }
        }

        return null;
    }

    /**
     * Normaliza un string removiendo tildes, caracteres especiales, guiones bajos y convirtiendo a minúsculas.
     */
    private String normalizeString(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", ""); // Remueve marcas diacríticas (acentos, tildes)
        normalized = normalized.replace("_", " ").replace("-", " ");
        return normalized.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private void resolveFastestLap(List<RaceResultBulkItemRequest> items) {
        if (items.isEmpty()) return;
        RaceResultBulkItemRequest fastest = null;
        Long minLapMs = Long.MAX_VALUE;

        for (RaceResultBulkItemRequest item : items) {
            Long ms = parseTimeToMillis(item.getBestLapTime());
            if (ms != null && ms < minLapMs && ms > 0) {
                minLapMs = ms;
                fastest = item;
            }
        }

        if (fastest != null) {
            fastest.setFastestLap(true);
        }
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

    private String getTagValue(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        return (nl != null && nl.getLength() > 0) ? nl.item(0).getTextContent().trim() : null;
    }

    private String formatMillis(Long ms) {
        if (ms == null || ms <= 0 || ms == 2147483647L) return null;
        long totalSeconds = ms / 1000;
        long millis = ms % 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }

    /**
     * Convierte un string en segundos (ej: "105.3559" o "1849.4352") a formato legible mm:ss.SSS / hh:mm:ss.SSS.
     */
    private String formatSecondsStringToLapTime(String secondsStr) {
        if (secondsStr == null || secondsStr.trim().isEmpty()) return null;
        String val = secondsStr.trim();
        // Si ya contiene dos puntos (ej: "01:45.355"), se retorna directamente
        if (val.contains(":")) return val;

        try {
            double totalSec = Double.parseDouble(val);
            if (totalSec <= 0 || totalSec >= 2147483647L) return null;

            long totalSeconds = (long) totalSec;
            long millis = Math.round((totalSec - totalSeconds) * 1000.0);
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            if (hours > 0) {
                return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis);
            } else {
                return String.format("%02d:%02d.%03d", minutes, seconds, millis);
            }
        } catch (Exception ignored) {
            return val;
        }
    }

    private Long parseTimeToMillis(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;
        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length == 3) {
                long hours = Long.parseLong(parts[0]);
                long min = Long.parseLong(parts[1]);
                double sec = Double.parseDouble(parts[2]);
                return (hours * 3600000L) + (min * 60000L) + (long) (sec * 1000.0);
            } else if (parts.length == 2) {
                long min = Long.parseLong(parts[0]);
                double sec = Double.parseDouble(parts[1]);
                return (min * 60000L) + (long) (sec * 1000.0);
            } else if (parts.length == 1) {
                double sec = Double.parseDouble(parts[0]);
                return (long) (sec * 1000.0);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
