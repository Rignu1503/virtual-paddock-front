package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.team.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

import java.util.UUID;

public interface ITeamService {
    TeamResponse create(TeamRequest request);
    TeamResponse getById(UUID id);
    PageResponse<TeamBasicResponse> getAll(int page, int size);
    PageResponse<TeamBasicResponse> getByLeagueId(UUID leagueId, int page, int size);
    TeamResponse update(UUID id, TeamUpdate update);
    void delete(UUID id);
}
