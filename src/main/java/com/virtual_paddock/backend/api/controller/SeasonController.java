package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.season.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISeasonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final ISeasonService seasonService;

    @PostMapping
    public ResponseEntity<ApiResponse<SeasonResponse>> create(@Valid @RequestBody SeasonRequest request) {
        SeasonResponse response = seasonService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Temporada creada exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeasonResponse>> getById(@PathVariable Long id) {
        SeasonResponse response = seasonService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeasonBasicResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SeasonBasicResponse> response = seasonService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/championship/{championshipId}")
    public ResponseEntity<ApiResponse<PageResponse<SeasonBasicResponse>>> getByChampionshipId(
            @PathVariable Long championshipId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SeasonBasicResponse> response = seasonService.getByChampionshipId(championshipId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeasonResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SeasonUpdate update) {
        SeasonResponse response = seasonService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Temporada actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        seasonService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Temporada eliminada exitosamente", null));
    }
}
