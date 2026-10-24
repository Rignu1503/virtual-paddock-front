package com.virtual_paddock.backend.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.infrastructure.abstract_service.ILiveTimingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/live-timing")
@RequiredArgsConstructor
public class LiveTimingController {

    private final ILiveTimingService liveTimingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Object>> getLiveTimingStatus() {
        JsonNode snapshot = liveTimingService.getLiveTimingSnapshot();
        try {
            Object converted = objectMapper.treeToValue(snapshot, Object.class);
            return ResponseEntity.ok(ApiResponse.ok("Telemetria obtenida exitosamente", converted));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.ok("Telemetria obtenida exitosamente", snapshot.toString()));
        }
    }

    @GetMapping(value = "/map", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTrackMap() {
        byte[] imageBytes = liveTimingService.getTrackMapImage();
        if (imageBytes != null && imageBytes.length > 0) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                    .body(imageBytes);
        }
        return ResponseEntity.notFound().build();
    }
}
