package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.season.*;
import com.virtual_paddock.backend.api.dtos.team.TeamResponse;

import java.util.List;
import java.util.UUID;

public interface ISeasonService {
    SeasonResponse create(SeasonRequest request);
    SeasonResponse getById(UUID id);
    PageResponse<SeasonBasicResponse> getAll(int page, int size);
    PageResponse<SeasonBasicResponse> getByChampionshipId(UUID championshipId, int page, int size);
    SeasonResponse update(UUID id, SeasonUpdate update);
    void delete(UUID id);

    SeasonResponse setDefaultSeason(UUID seasonId);
    List<TeamResponse> getTeamsBySeasonId(UUID seasonId);
}
