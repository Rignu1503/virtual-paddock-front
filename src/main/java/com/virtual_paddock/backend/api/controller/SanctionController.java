package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.sanction.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISanctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sanctions")
@RequiredArgsConstructor
public class SanctionController {

    private final ISanctionService sanctionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<SanctionResponse>> create(@Valid @RequestBody SanctionRequest request) {
        SanctionResponse response = sanctionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Sanción creada exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SanctionResponse>> getById(@PathVariable UUID id) {
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
            @PathVariable UUID raceEventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SanctionBasicResponse> response = sanctionService.getByRaceEventId(raceEventId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<PageResponse<SanctionBasicResponse>>> getByDriverId(
            @PathVariable UUID driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SanctionBasicResponse> response = sanctionService.getByDriverId(driverId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<SanctionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SanctionUpdate update) {
        SanctionResponse response = sanctionService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Sanción actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sanctionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Sanción eliminada exitosamente", null));
    }
}
