package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.team.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.ITeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final ITeamService teamService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> create(@Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Equipo creado exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getById(@PathVariable Long id) {
        TeamResponse response = teamService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TeamBasicResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TeamBasicResponse> response = teamService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<ApiResponse<PageResponse<TeamBasicResponse>>> getByLeagueId(
            @PathVariable Long leagueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TeamBasicResponse> response = teamService.getByLeagueId(leagueId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TeamUpdate update) {
        TeamResponse response = teamService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Equipo actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Equipo eliminado exitosamente", null));
    }
}
