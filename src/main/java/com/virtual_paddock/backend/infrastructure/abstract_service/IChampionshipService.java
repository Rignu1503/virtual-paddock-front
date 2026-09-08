package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.championship.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

import java.util.UUID;

public interface IChampionshipService {
    ChampionshipResponse create(ChampionshipRequest request);
    ChampionshipResponse getById(UUID id);
    PageResponse<ChampionshipBasicResponse> getAll(int page, int size);
    PageResponse<ChampionshipBasicResponse> getByLeagueId(UUID leagueId, int page, int size);
    ChampionshipResponse update(UUID id, ChampionshipUpdate update);
    void delete(UUID id);
}
