package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.raceevent.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/race-events")
@RequiredArgsConstructor
public class RaceEventController {

    private final IRaceEventService raceEventService;

    @PostMapping
    public ResponseEntity<ApiResponse<RaceEventResponse>> create(@Valid @RequestBody RaceEventRequest request) {
        RaceEventResponse response = raceEventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Evento de carrera creado exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RaceEventResponse>> getById(@PathVariable Long id) {
        RaceEventResponse response = raceEventService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RaceEventBasicResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<RaceEventBasicResponse> response = raceEventService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/season/{seasonId}")
    public ResponseEntity<ApiResponse<PageResponse<RaceEventBasicResponse>>> getBySeasonId(
            @PathVariable Long seasonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<RaceEventBasicResponse> response = raceEventService.getBySeasonId(seasonId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RaceEventResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RaceEventUpdate update) {
        RaceEventResponse response = raceEventService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Evento de carrera actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        raceEventService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Evento de carrera eliminado exitosamente", null));
    }
}
