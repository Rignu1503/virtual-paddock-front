package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.raceresult.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final IRaceResultService raceResultService;
    private final com.virtual_paddock.backend.infrastructure.abstract_service.ISimulatorImportService simulatorImportService;

    @PostMapping
    public ResponseEntity<ApiResponse<RaceResultResponse>> create(@Valid @RequestBody RaceResultRequest request) {
        RaceResultResponse response = raceResultService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Resultado creado exitosamente", response));
    }

    @PostMapping("/race-event/{raceEventId}/batch")
    public ResponseEntity<ApiResponse<java.util.List<RaceResultResponse>>> processBatchResults(
            @PathVariable Long raceEventId,
            @Valid @RequestBody RaceResultBulkRequest request) {
        java.util.List<RaceResultResponse> response = raceResultService.processBatchResults(raceEventId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Resultados calculados y guardados exitosamente", response));
    }

    @PostMapping("/race-event/{raceEventId}/preview")
    public ResponseEntity<ApiResponse<java.util.List<RaceResultResponse>>> previewBatchResults(
            @PathVariable Long raceEventId,
            @Valid @RequestBody RaceResultBulkRequest request) {
        java.util.List<RaceResultResponse> response = raceResultService.previewBatchResults(raceEventId, request);
        return ResponseEntity.ok(ApiResponse.ok("Previsualización de resultados generada exitosamente", response));
    }

    @PostMapping(value = "/race-event/{raceEventId}/preview-import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<java.util.List<RaceResultResponse>>> previewImportFile(
            @PathVariable Long raceEventId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        RaceResultBulkRequest bulkRequest = simulatorImportService.parseSimulatorFile(file, raceEventId);
        java.util.List<RaceResultResponse> response = raceResultService.previewBatchResults(raceEventId, bulkRequest);
        return ResponseEntity.ok(ApiResponse.ok("Previsualización del archivo generada exitosamente", response));
    }

    @PostMapping(value = "/race-event/{raceEventId}/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<java.util.List<RaceResultResponse>>> importFileAndSave(
            @PathVariable Long raceEventId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        RaceResultBulkRequest bulkRequest = simulatorImportService.parseSimulatorFile(file, raceEventId);
        java.util.List<RaceResultResponse> response = raceResultService.processBatchResults(raceEventId, bulkRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Resultados importados y guardados exitosamente", response));
    }

    @PostMapping("/race-event/{raceEventId}/recalculate")
    public ResponseEntity<ApiResponse<java.util.List<RaceResultResponse>>> recalculateEventStandings(
            @PathVariable Long raceEventId) {
        java.util.List<RaceResultResponse> response = raceResultService.recalculateEventStandings(raceEventId);
        return ResponseEntity.ok(ApiResponse.ok("Posiciones y puntos recalculados exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RaceResultResponse>> getById(@PathVariable Long id) {
        RaceResultResponse response = raceResultService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/race-event/{raceEventId}")
    public ResponseEntity<ApiResponse<PageResponse<RaceResultBasicResponse>>> getByRaceEventId(
            @PathVariable Long raceEventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<RaceResultBasicResponse> response = raceResultService.getByRaceEventId(raceEventId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<PageResponse<RaceResultBasicResponse>>> getByDriverId(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<RaceResultBasicResponse> response = raceResultService.getByDriverId(driverId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RaceResultResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RaceResultUpdate update) {
        RaceResultResponse response = raceResultService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Resultado actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        raceResultService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Resultado eliminado exitosamente", null));
    }
}
