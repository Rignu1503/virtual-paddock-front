package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.sanction.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISanctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sanctions")
@RequiredArgsConstructor
public class SanctionController {

    private final ISanctionService sanctionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SanctionResponse>> create(@Valid @RequestBody SanctionRequest request) {
        SanctionResponse response = sanctionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Sanción creada exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SanctionResponse>> getById(@PathVariable Long id) {
        SanctionResponse response = sanctionService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SanctionBasicResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SanctionBasicResponse> response = sanctionService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/race-event/{raceEventId}")
    public ResponseEntity<ApiResponse<PageResponse<SanctionBasicResponse>>> getByRaceEventId(
            @PathVariable Long raceEventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SanctionBasicResponse> response = sanctionService.getByRaceEventId(raceEventId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<PageResponse<SanctionBasicResponse>>> getByDriverId(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SanctionBasicResponse> response = sanctionService.getByDriverId(driverId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SanctionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SanctionUpdate update) {
        SanctionResponse response = sanctionService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Sanción actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        sanctionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Sanción eliminada exitosamente", null));
    }
}
