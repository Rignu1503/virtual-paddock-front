package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.championship.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

public interface IChampionshipService {
    ChampionshipResponse create(ChampionshipRequest request);
    ChampionshipResponse getById(Long id);
    PageResponse<ChampionshipBasicResponse> getAll(int page, int size);
    PageResponse<ChampionshipBasicResponse> getByLeagueId(Long leagueId, int page, int size);
    ChampionshipResponse update(Long id, ChampionshipUpdate update);
    void delete(Long id);
}
