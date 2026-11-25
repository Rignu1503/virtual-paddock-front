package com.virtual_paddock.backend.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.virtual_paddock.backend.infrastructure.abstract_service.ILiveTimingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LiveTimingServiceImpl implements ILiveTimingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    @Value("${application.live-timing.http-url:https://usa2.assettohosting.com:50508/public/live.json}")
    private String liveTimingHttpUrl;

    @Value("${application.live-timing.ws-url:wss://usa2.assettohosting.com:50508/api/race-control}")
    private String liveTimingWsUrl;

    @Value("${application.live-timing.origin:https://usa2.assettohosting.com:50508}")
    private String liveTimingOrigin;

    private volatile JsonNode cachedSnapshot = null;
    private volatile long lastFetchTime = 0L;
    private static final long CACHE_TTL_MS = 2500L;

    public LiveTimingServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public JsonNode getLiveTimingSnapshot() {
        long now = System.currentTimeMillis();
        if (cachedSnapshot != null && (now - lastFetchTime) < CACHE_TTL_MS) {
            return cachedSnapshot;
        }

        synchronized (this) {
            now = System.currentTimeMillis();
            if (cachedSnapshot != null && (now - lastFetchTime) < CACHE_TTL_MS) {
                return cachedSnapshot;
            }

            try {
                JsonNode freshSnapshot = fetchSnapshotFromAssettoHosting();
                if (freshSnapshot != null) {
                    this.cachedSnapshot = freshSnapshot;
                    this.lastFetchTime = System.currentTimeMillis();
                    return freshSnapshot;
                }
            } catch (Exception e) {
                log.warn("Error consultando telemetria en vivo de AssettoHosting: {}", e.getMessage());
            }

            if (this.cachedSnapshot != null) {
                return this.cachedSnapshot;
            }

            ObjectNode fallback = objectMapper.createObjectNode();
            fallback.put("status", "disconnected");
            fallback.put("message", "No fue posible conectar con el servidor de telemetria");
            return fallback;
        }
    }

    private JsonNode fetchSnapshotFromAssettoHosting() {
        // 1. Intentar endpoint HTTP /public/live.json de AssettoHosting
        JsonNode httpSnapshot = fetchHttpSnapshot();
        if (httpSnapshot != null) {
            return httpSnapshot;
        }

        // 2. Fallback a WebSocket si HTTP no responde
        try {
            return fetchWsSnapshot();
        } catch (Exception e) {
            log.debug("WebSocket fallback de telemetria fallo: {}", e.getMessage());
        }

        return null;
    }

    private JsonNode fetchHttpSnapshot() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(liveTimingHttpUrl))
                    .timeout(Duration.ofSeconds(3))
                    .header("User-Agent", "VirtualPaddock/2.0")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null && !response.body().isBlank()) {
                JsonNode raw = objectMapper.readTree(response.body());
                return transformPublicLiveJson(raw);
            }
        } catch (Exception e) {
            log.warn("Error consultando endpoint HTTP de telemetria ({}): {}", liveTimingHttpUrl, e.getMessage());
        }
        return null;
    }

    private JsonNode transformPublicLiveJson(JsonNode raw) {
        if (raw == null || !raw.isObject()) {
            return raw;
        }

        ObjectNode root = raw.deepCopy();

        // 1. Construir SessionInfo compatible con LiveTimingMessage
        ObjectNode sessionInfo = objectMapper.createObjectNode();
        sessionInfo.put("ServerName", "fda'server");

        JsonNode sessionNode = raw.get("session");
        if (sessionNode != null && sessionNode.isObject()) {
            String sName = sessionNode.has("name") ? sessionNode.get("name").asText("Practice") : "Practice";
            String sType = sessionNode.has("type") ? sessionNode.get("type").asText("Practice") : sName;
            String track = sessionNode.has("track") ? sessionNode.get("track").asText("spa") : "spa";

            sessionInfo.put("Name", sName);
            sessionInfo.put("Track", track);
            sessionInfo.put("TrackConfig", sessionNode.has("layout") ? sessionNode.get("layout").asText("") : "");

            int typeCode = 1;
            if (sType.equalsIgnoreCase("Qualifying") || sType.equalsIgnoreCase("Qualify")) {
                typeCode = 2;
            } else if (sType.equalsIgnoreCase("Race")) {
                typeCode = 3;
            }
            sessionInfo.put("Type", typeCode);

            long timeLeftSec = sessionNode.has("timeLeftSec") ? sessionNode.get("timeLeftSec").asLong(0L) : 0L;
            sessionInfo.put("Time", timeLeftSec);
            sessionInfo.put("Laps", sessionNode.has("laps") ? sessionNode.get("laps").asInt(0) : 0);
            sessionInfo.put("AmbientTemp", sessionNode.has("ambient") ? sessionNode.get("ambient").asDouble(25.0) : 25.0);
            sessionInfo.put("RoadTemp", sessionNode.has("road") ? sessionNode.get("road").asDouble(37.0) : 37.0);
            sessionInfo.put("WeatherGraphics", sessionNode.has("weatherName") ? sessionNode.get("weatherName").asText("") : "");
        } else {
            sessionInfo.put("Name", "Practice");
            sessionInfo.put("Track", "spa");
            sessionInfo.put("Type", 1);
            sessionInfo.put("Time", 0);
            sessionInfo.put("Laps", 0);
            sessionInfo.put("AmbientTemp", 25.0);
            sessionInfo.put("RoadTemp", 37.0);
        }
        root.set("SessionInfo", sessionInfo);

        // 2. Construir TrackInfo
        ObjectNode trackInfo = objectMapper.createObjectNode();
        String trackName = raw.has("trackName") ? raw.get("trackName").asText("Spa") : "Spa";
        trackInfo.put("name", trackName);
        if (raw.has("trackLength")) {
            trackInfo.put("length", raw.get("trackLength").asText("7004"));
        }
        root.set("TrackInfo", trackInfo);

        // 3. Construir ConnectedDrivers y DisconnectedDrivers
        ObjectNode connectedDrivers = objectMapper.createObjectNode();
        ObjectNode connectedDriversMap = objectMapper.createObjectNode();
        ArrayNode connectedGuidsOrder = objectMapper.createArrayNode();

        ObjectNode disconnectedDrivers = objectMapper.createObjectNode();
        ObjectNode disconnectedDriversMap = objectMapper.createObjectNode();
        ArrayNode disconnectedGuidsOrder = objectMapper.createArrayNode();

        JsonNode playersNode = raw.get("players");
        if (playersNode != null && playersNode.isArray()) {
            java.util.List<ObjectNode> connectedList = new java.util.ArrayList<>();
            java.util.Map<String, ObjectNode> disconnectedMap = new java.util.LinkedHashMap<>();

            for (JsonNode p : playersNode) {
                if (p == null || !p.isObject()) continue;
                String name = p.has("name") ? p.get("name").asText("Piloto") : "Piloto";
                String guid = p.has("guid") ? p.get("guid").asText(name) : (p.has("id") ? p.get("id").asText(name) : name);

                // Extracción de modelo de vehículo (AssettoHosting usa "model", otros usan "carModel" o "car")
                String carModel = "";
                if (p.has("model") && !p.get("model").asText().isBlank()) {
                    carModel = p.get("model").asText().trim();
                } else if (p.has("carModel") && !p.get("carModel").asText().isBlank()) {
                    carModel = p.get("carModel").asText().trim();
                } else if (p.has("car") && !p.get("car").asText().isBlank()) {
                    carModel = p.get("car").asText().trim();
                }

                // Extracción de nombre legible de vehículo (AssettoHosting usa "modelName", otros "carName")
                String carName = "";
                if (p.has("modelName") && !p.get("modelName").asText().isBlank()) {
                    carName = p.get("modelName").asText().trim();
                } else if (p.has("carName") && !p.get("carName").asText().isBlank()) {
                    carName = p.get("carName").asText().trim();
                } else if (p.has("skinName") && !p.get("skinName").asText().isBlank()) {
                    carName = p.get("skinName").asText().trim();
                }
                if (carName.isBlank()) {
                    carName = carModel.isBlank() ? "Vehículo Oficial" : carModel;
                }

                // Detección de piloto desconectado / stint histórico
                boolean isDisconnected = false;
                if (p.has("status") && "gone".equalsIgnoreCase(p.get("status").asText())) {
                    isDisconnected = true;
                }
                if (p.has("loaded") && !p.get("loaded").asBoolean()) {
                    isDisconnected = true;
                }
                if (p.has("id") && p.get("id").asInt() < 0) {
                    isDisconnected = true;
                }

                ObjectNode dNode = objectMapper.createObjectNode();
                dNode.put("Split", p.has("gap") ? p.get("gap").asText("-") : "-");

                ObjectNode carInfo = objectMapper.createObjectNode();
                carInfo.put("DriverName", name);
                carInfo.put("DriverGUID", guid);
                carInfo.put("CarModel", carModel);
                carInfo.put("CarName", carName);
                dNode.set("CarInfo", carInfo);

                ObjectNode cars = objectMapper.createObjectNode();
                ObjectNode carData = objectMapper.createObjectNode();
                carData.put("CarName", carName);

                long bestLapNs = parseTimeToNs(p.has("bestLap") ? p.get("bestLap").asText("") : "");
                long lastLapNs = parseTimeToNs(p.has("lastLap") ? p.get("lastLap").asText("") : "");
                carData.put("BestLap", bestLapNs);
                carData.put("LastLap", lastLapNs);
                carData.put("NumLaps", p.has("laps") ? p.get("laps").asInt(0) : 0);
                carData.put("TopSpeedBestLap", p.has("speed") ? p.get("speed").asDouble(0.0) : 0.0);

                if (!carModel.isBlank()) {
                    cars.set(carModel, carData);
                }
                cars.set("default", carData);
                dNode.set("Cars", cars);

                if (!isDisconnected) {
                    dNode.put("Position", p.has("pos") ? p.get("pos").asInt(connectedList.size() + 1) : (connectedList.size() + 1));
                    connectedList.add(dNode);
                } else {
                    String cleanKey = name.trim().toLowerCase();
                    if (!disconnectedMap.containsKey(cleanKey)) {
                        disconnectedMap.put(cleanKey, dNode);
                    } else {
                        ObjectNode existing = disconnectedMap.get(cleanKey);
                        long existingBestLap = existing.path("Cars").path("default").path("BestLap").asLong(0L);
                        int existingLaps = existing.path("Cars").path("default").path("NumLaps").asInt(0);
                        int newLaps = carData.path("NumLaps").asInt(0);

                        if (bestLapNs > 0 && (existingBestLap == 0L || bestLapNs < existingBestLap)) {
                            carData.put("NumLaps", existingLaps + newLaps);
                            disconnectedMap.put(cleanKey, dNode);
                        } else {
                            JsonNode existingDefault = existing.path("Cars").path("default");
                            if (existingDefault.isObject()) {
                                ((ObjectNode) existingDefault).put("NumLaps", existingLaps + newLaps);
                            }
                        }
                    }
                }
            }

            // 1. Llenar ConnectedDrivers
            int connPos = 1;
            for (ObjectNode node : connectedList) {
                String guid = node.path("CarInfo").path("DriverGUID").asText("PILOT_" + connPos);
                node.put("Position", connPos);
                connectedDriversMap.set(guid, node);
                connectedGuidsOrder.add(guid);
                connPos++;
            }

            // 2. Ordenar y llenar DisconnectedDrivers por mejor vuelta
            java.util.List<ObjectNode> discList = new java.util.ArrayList<>(disconnectedMap.values());
            discList.sort((a, b) -> {
                long lapA = a.path("Cars").path("default").path("BestLap").asLong(0L);
                long lapB = b.path("Cars").path("default").path("BestLap").asLong(0L);
                if (lapA > 0 && lapB > 0) return Long.compare(lapA, lapB);
                if (lapA > 0) return -1;
                if (lapB > 0) return 1;
                return Integer.compare(b.path("Cars").path("default").path("NumLaps").asInt(0),
                        a.path("Cars").path("default").path("NumLaps").asInt(0));
            });

            long leaderLapNs = discList.isEmpty() ? 0L : discList.get(0).path("Cars").path("default").path("BestLap").asLong(0L);
            int discPos = 1;
            for (ObjectNode node : discList) {
                String guid = node.path("CarInfo").path("DriverGUID").asText("DISC_" + discPos);
                node.put("Position", discPos);

                long bestLap = node.path("Cars").path("default").path("BestLap").asLong(0L);
                if (discPos == 1 || leaderLapNs == 0L || bestLap == 0L) {
                    node.put("Split", "--");
                } else {
                    double gapSec = (bestLap - leaderLapNs) / 1_000_000_000.0;
                    node.put("Split", String.format(java.util.Locale.US, "+%.3f", gapSec));
                }

                disconnectedDriversMap.set(guid, node);
                disconnectedGuidsOrder.add(guid);
                discPos++;
            }
        }

        connectedDrivers.set("Drivers", connectedDriversMap);
        connectedDrivers.set("GUIDsInPositionalOrder", connectedGuidsOrder);
        root.set("ConnectedDrivers", connectedDrivers);

        disconnectedDrivers.set("Drivers", disconnectedDriversMap);
        disconnectedDrivers.set("GUIDsInPositionalOrder", disconnectedGuidsOrder);
        root.set("DisconnectedDrivers", disconnectedDrivers);

        return root;
    }

    private long parseTimeToNs(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return 0L;
        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length == 2) {
                long minutes = Long.parseLong(parts[0].trim());
                double seconds = Double.parseDouble(parts[1].trim());
                return (long) ((minutes * 60.0 + seconds) * 1_000_000_000.0);
            } else if (parts.length == 1) {
                double seconds = Double.parseDouble(parts[0].trim());
                return (long) (seconds * 1_000_000_000.0);
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }

    private JsonNode fetchWsSnapshot() throws Exception {
        CompletableFuture<String> payloadFuture = new CompletableFuture<>();
        StringBuilder buffer = new StringBuilder();

        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                buffer.append(data);
                if (last) {
                    payloadFuture.complete(buffer.toString());
                    try {
                        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Snapshot received");
                    } catch (Exception ignored) {
                    }
                }
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                payloadFuture.completeExceptionally(error);
                WebSocket.Listener.super.onError(webSocket, error);
            }
        };

        httpClient.newWebSocketBuilder()
                .header("Origin", liveTimingOrigin)
                .connectTimeout(Duration.ofSeconds(3))
                .buildAsync(URI.create(liveTimingWsUrl), listener)
                .join();

        String rawJson = payloadFuture.get(4, TimeUnit.SECONDS);
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }

        JsonNode root = objectMapper.readTree(rawJson);
        if (root.has("Message")) {
            return root.get("Message");
        }
        return root;
    }
}
