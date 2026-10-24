package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimRequest;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimResponse;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimReviewRequest;
import com.virtual_paddock.backend.infrastructure.abstract_service.IIncidentClaimService;
import com.virtual_paddock.backend.utils.enums.IncidentClaimStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incident-claims")
@RequiredArgsConstructor
public class IncidentClaimController {

    private final IIncidentClaimService incidentClaimService;

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentClaimResponse>> create(@Valid @RequestBody IncidentClaimRequest request) {
        IncidentClaimResponse response = incidentClaimService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Reclamo de incidente registrado exitosamente", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<IncidentClaimResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) UUID raceEventId,
            @RequestParam(required = false) IncidentClaimStatus status) {
        PageResponse<IncidentClaimResponse> response = incidentClaimService.getAll(page, size, raceEventId, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/race-event/{raceEventId}")
    public ResponseEntity<ApiResponse<List<IncidentClaimResponse>>> getByRaceEvent(@PathVariable UUID raceEventId) {
        List<IncidentClaimResponse> response = incidentClaimService.getAllByRaceEvent(raceEventId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentClaimResponse>> getById(@PathVariable UUID id) {
        IncidentClaimResponse response = incidentClaimService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<IncidentClaimResponse>> review(
            @PathVariable UUID id,
            @Valid @RequestBody IncidentClaimReviewRequest request) {
        IncidentClaimResponse response = incidentClaimService.review(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Veredicto de comisaría guardado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        incidentClaimService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Expediente de incidente eliminado exitosamente", null));
    }
}
