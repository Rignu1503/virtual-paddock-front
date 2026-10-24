package com.virtual_paddock.backend.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.infrastructure.abstract_service.ILiveTimingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
