package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.driver.DriverResponse;
import com.virtual_paddock.backend.api.dtos.driver_registration.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IDriverRegistrationService;
import com.virtual_paddock.backend.utils.enums.DriverRegistrationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/driver-registrations")
@RequiredArgsConstructor
public class DriverRegistrationController {

    private final IDriverRegistrationService registrationService;

    // Inscripción pública abierta (sin autenticación requerida)
    @PostMapping
    public ResponseEntity<ApiResponse<DriverRegistrationResponse>> register(
            @Valid @RequestBody DriverRegistrationRequest request
    ) {
        DriverRegistrationResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Inscripción recibida con éxito. Está en espera de aprobación por comisarios.", response));
    }

    // Listar solicitudes (Administración)
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<DriverRegistrationResponse>>> getAll(
            @RequestParam(required = false) UUID leagueId,
            @RequestParam(required = false) UUID championshipId,
            @RequestParam(required = false) DriverRegistrationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<DriverRegistrationResponse> response = registrationService.getFiltered(leagueId, championshipId, status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // Detalle de solicitud (Administración)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<DriverRegistrationResponse>> getById(@PathVariable UUID id) {
        DriverRegistrationResponse response = registrationService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // Aprobar solicitud (Administración)
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<DriverResponse>> approve(
            @PathVariable UUID id,
            @Valid @RequestBody DriverApprovalRequest approval
    ) {
        DriverResponse driver = registrationService.approve(id, approval);
        return ResponseEntity.ok(ApiResponse.ok("Piloto aprobado e incorporado exitosamente a la competición", driver));
    }

    // Rechazar solicitud (Administración)
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<DriverRegistrationResponse>> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) DriverRejectionRequest rejection
    ) {
        DriverRegistrationResponse response = registrationService.reject(id, rejection);
        return ResponseEntity.ok(ApiResponse.ok("Solicitud de inscripción rechazada", response));
    }

    // Conteo de pendientes (Administración / Dashboard)
    @GetMapping("/count-pending")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countPending(
            @RequestParam(required = false) UUID leagueId
    ) {
        long count = registrationService.countPending(leagueId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }
}
