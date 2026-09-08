package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.raceresult.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceResultService;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISimulatorImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final IRaceResultService raceResultService;
    private final ISimulatorImportService simulatorImportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<RaceResultResponse>> create(@Valid @RequestBody RaceResultRequest request) {
        RaceResultResponse response = raceResultService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Resultado creado exitosamente", response));
    }

    @PostMapping("/race-event/{raceEventId}/batch")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<List<RaceResultResponse>>> processBatchResults(
            @PathVariable UUID raceEventId,
            @Valid @RequestBody RaceResultBulkRequest request) {
        List<RaceResultResponse> response = raceResultService.processBatchResults(raceEventId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Resultados calculados y guardados exitosamente", response));
    }

    @PostMapping("/race-event/{raceEventId}/preview")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<List<RaceResultResponse>>> previewBatchResults(
            @PathVariable UUID raceEventId,
            @Valid @RequestBody RaceResultBulkRequest request) {
        List<RaceResultResponse> response = raceResultService.previewBatchResults(raceEventId, request);
        return ResponseEntity.ok(ApiResponse.ok("Previsualización de resultados generada exitosamente", response));
    }

    @PostMapping(value = "/race-event/{raceEventId}/preview-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<List<RaceResultResponse>>> previewImportFile(
            @PathVariable UUID raceEventId,
            @RequestParam("file") MultipartFile file) {
        RaceResultBulkRequest bulkRequest = simulatorImportService.parseSimulatorFile(file, raceEventId);
        List<RaceResultResponse> response = raceResultService.previewBatchResults(raceEventId, bulkRequest);
        return ResponseEntity.ok(ApiResponse.ok("Previsualización del archivo generada exitosamente", response));
    }

    @PostMapping(value = "/race-event/{raceEventId}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<List<RaceResultResponse>>> importFileAndSave(
            @PathVariable UUID raceEventId,
            @RequestParam("file") MultipartFile file) {
        RaceResultBulkRequest bulkRequest = simulatorImportService.parseSimulatorFile(file, raceEventId);
        List<RaceResultResponse> response = raceResultService.processBatchResults(raceEventId, bulkRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Resultados importados y guardados exitosamente", response));
    }

    @PostMapping("/race-event/{raceEventId}/recalculate")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<List<RaceResultResponse>>> recalculateEventStandings(
            @PathVariable UUID raceEventId) {
        List<RaceResultResponse> response = raceResultService.recalculateEventStandings(raceEventId);
        return ResponseEntity.ok(ApiResponse.ok("Posiciones y puntos recalculados exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RaceResultResponse>> getById(@PathVariable UUID id) {
        RaceResultResponse response = raceResultService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/race-event/{raceEventId}")
    public ResponseEntity<ApiResponse<PageResponse<RaceResultBasicResponse>>> getByRaceEventId(
            @PathVariable UUID raceEventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<RaceResultBasicResponse> response = raceResultService.getByRaceEventId(raceEventId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<PageResponse<RaceResultBasicResponse>>> getByDriverId(
            @PathVariable UUID driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<RaceResultBasicResponse> response = raceResultService.getByDriverId(driverId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<RaceResultResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RaceResultUpdate update) {
        RaceResultResponse response = raceResultService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Resultado actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        raceResultService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Resultado eliminado exitosamente", null));
    }
}
