package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.standings.DriverStandingResponse;
import com.virtual_paddock.backend.api.dtos.standings.TeamStandingResponse;

import java.util.List;
import java.util.UUID;

public interface IStandingsService {
    List<DriverStandingResponse> getDriverStandings(UUID seasonId, String category);
    List<TeamStandingResponse> getTeamStandings(UUID seasonId, String category);
}
