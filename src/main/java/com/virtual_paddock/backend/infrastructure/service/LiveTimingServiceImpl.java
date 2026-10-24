package com.virtual_paddock.backend.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Override
    public byte[] getTrackMapImage() {
        JsonNode snapshot = getLiveTimingSnapshot();
        if (snapshot == null) return null;

        String track = "";
        String config = "";

        if (snapshot.has("SessionInfo")) {
            JsonNode sInfo = snapshot.get("SessionInfo");
            track = sInfo.has("Track") ? sInfo.get("Track").asText() : "";
            config = sInfo.has("TrackConfig") ? sInfo.get("TrackConfig").asText() : "";
        }

        if (track.isEmpty()) {
            track = "lilski_watkins_glen";
            config = "boot";
        }

        String path = "/content/tracks/" + track + (!config.isEmpty() ? "/" + config : "") + "/map.png";
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(liveTimingOrigin + path))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<byte[]> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() == 200) {
                return resp.body();
            }
        } catch (Exception e) {
            log.warn("Error al descargar mapa del circuito {}: {}", path, e.getMessage());
        }
        return null;
    }

    private JsonNode fetchSnapshotFromAssettoHosting() throws Exception {
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
