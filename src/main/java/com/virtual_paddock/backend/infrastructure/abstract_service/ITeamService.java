package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.team.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

public interface ITeamService {
    TeamResponse create(TeamRequest request);
    TeamResponse getById(Long id);
    PageResponse<TeamBasicResponse> getAll(int page, int size);
    PageResponse<TeamBasicResponse> getByLeagueId(Long leagueId, int page, int size);
    TeamResponse update(Long id, TeamUpdate update);
    void delete(Long id);
}
