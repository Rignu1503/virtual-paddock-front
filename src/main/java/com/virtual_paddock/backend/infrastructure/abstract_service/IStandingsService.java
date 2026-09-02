package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.standings.DriverStandingResponse;
import com.virtual_paddock.backend.api.dtos.standings.TeamStandingResponse;

import java.util.List;

public interface IStandingsService {
    List<DriverStandingResponse> getDriverStandings(Long seasonId, String category);
    List<TeamStandingResponse> getTeamStandings(Long seasonId, String category);
}
