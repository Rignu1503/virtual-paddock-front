package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.league.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.ILeagueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final ILeagueService leagueService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<LeagueResponse>> create(@Valid @RequestBody LeagueRequest request) {
        LeagueResponse response = leagueService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Liga creada exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeagueResponse>> getById(@PathVariable UUID id) {
        LeagueResponse response = leagueService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/slug/{slugUrl}")
    public ResponseEntity<ApiResponse<LeagueResponse>> getBySlugUrl(@PathVariable String slugUrl) {
        LeagueResponse response = leagueService.getBySlugUrl(slugUrl);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LeagueBasicResponse>>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        PageResponse<LeagueBasicResponse> response = leagueService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<LeagueResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeagueUpdate update) {
        LeagueResponse response = leagueService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Liga actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        leagueService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Liga eliminada exitosamente", null));
    }
}
