package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.pointssystem.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IPointsSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points-systems")
@RequiredArgsConstructor
public class PointsSystemController {

    private final IPointsSystemService pointsSystemService;

    @PostMapping
    public ResponseEntity<ApiResponse<PointsSystemResponse>> create(@Valid @RequestBody PointsSystemRequest request) {
        PointsSystemResponse response = pointsSystemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Sistema de puntos creado exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PointsSystemResponse>> getById(@PathVariable Long id) {
        PointsSystemResponse response = pointsSystemService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<ApiResponse<List<PointsSystemResponse>>> getByLeagueId(@PathVariable Long leagueId) {
        List<PointsSystemResponse> response = pointsSystemService.getByLeagueId(leagueId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/league/{leagueId}/paged")
    public ResponseEntity<ApiResponse<PageResponse<PointsSystemBasicResponse>>> getByLeagueIdPaged(
            @PathVariable Long leagueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<PointsSystemBasicResponse> response = pointsSystemService.getByLeagueIdPaged(leagueId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PointsSystemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PointsSystemUpdate update) {
        PointsSystemResponse response = pointsSystemService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Sistema de puntos actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        pointsSystemService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Sistema de puntos eliminado exitosamente", null));
    }
}
