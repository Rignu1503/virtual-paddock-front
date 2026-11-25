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

        // 3. Construir ConnectedDrivers
        ObjectNode connectedDrivers = objectMapper.createObjectNode();
        ObjectNode driversMap = objectMapper.createObjectNode();
        ArrayNode guidsOrder = objectMapper.createArrayNode();

        JsonNode playersNode = raw.get("players");
        if (playersNode != null && playersNode.isArray()) {
            int pos = 1;
            for (JsonNode p : playersNode) {
                if (p == null || !p.isObject()) continue;
                String name = p.has("name") ? p.get("name").asText("Piloto") : "Piloto " + pos;
                String guid = p.has("guid") ? p.get("guid").asText(name) : (p.has("id") ? p.get("id").asText(name) : name);
                String carModel = p.has("carModel") ? p.get("carModel").asText("") : (p.has("car") ? p.get("car").asText("") : (p.has("carName") ? p.get("carName").asText("") : ""));
                String carName = p.has("carName") ? p.get("carName").asText(carModel) : carModel;

                ObjectNode dNode = objectMapper.createObjectNode();
                dNode.put("Position", p.has("pos") ? p.get("pos").asInt(pos) : pos);
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

                cars.set(carModel.isBlank() ? "default" : carModel, carData);
                dNode.set("Cars", cars);

                driversMap.set(guid, dNode);
                guidsOrder.add(guid);
                pos++;
            }
        }

        connectedDrivers.set("Drivers", driversMap);
        connectedDrivers.set("GUIDsInPositionalOrder", guidsOrder);
        root.set("ConnectedDrivers", connectedDrivers);

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
