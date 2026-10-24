package com.virtual_paddock.backend.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
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

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<JsonNode>> getLiveTimingStatus() {
        JsonNode snapshot = liveTimingService.getLiveTimingSnapshot();
        return ResponseEntity.ok(ApiResponse.ok("Telemetria obtenida exitosamente", snapshot));
    }
}
