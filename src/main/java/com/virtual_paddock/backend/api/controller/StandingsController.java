package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.standings.DriverStandingResponse;
import com.virtual_paddock.backend.api.dtos.standings.TeamStandingResponse;
import com.virtual_paddock.backend.infrastructure.abstract_service.IStandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seasons/{seasonId}/standings")
@RequiredArgsConstructor
public class StandingsController {

    private final IStandingsService standingsService;

    @GetMapping("/drivers")
    public ResponseEntity<ApiResponse<List<DriverStandingResponse>>> getDriverStandings(
            @PathVariable UUID seasonId,
            @RequestParam(required = false) String category) {
        
        List<DriverStandingResponse> standings = standingsService.getDriverStandings(seasonId, category);
        return ResponseEntity.ok(ApiResponse.ok("Clasificación de pilotos obtenida exitosamente", standings));
    }

    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<List<TeamStandingResponse>>> getTeamStandings(
            @PathVariable UUID seasonId,
            @RequestParam(required = false) String category) {
        
        List<TeamStandingResponse> standings = standingsService.getTeamStandings(seasonId, category);
        return ResponseEntity.ok(ApiResponse.ok("Clasificación de equipos obtenida exitosamente", standings));
    }
}
