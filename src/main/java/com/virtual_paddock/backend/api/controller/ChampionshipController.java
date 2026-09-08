package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.championship.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IChampionshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/championships")
@RequiredArgsConstructor
public class ChampionshipController {

    private final IChampionshipService championshipService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<ChampionshipResponse>> create(@Valid @RequestBody ChampionshipRequest request) {
        ChampionshipResponse response = championshipService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Campeonato creado exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChampionshipResponse>> getById(@PathVariable UUID id) {
        ChampionshipResponse response = championshipService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ChampionshipBasicResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ChampionshipBasicResponse> response = championshipService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<ApiResponse<PageResponse<ChampionshipBasicResponse>>> getByLeagueId(
            @PathVariable UUID leagueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ChampionshipBasicResponse> response = championshipService.getByLeagueId(leagueId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<ChampionshipResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ChampionshipUpdate update) {
        ChampionshipResponse response = championshipService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Campeonato actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        championshipService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Campeonato eliminado exitosamente", null));
    }
}
