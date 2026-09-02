package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.league.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

public interface ILeagueService {
    LeagueResponse create(LeagueRequest request);
    LeagueResponse getById(Long id);
    LeagueResponse getBySlugUrl(String slugUrl);
    PageResponse<LeagueBasicResponse> getAll(int page, int size);
    LeagueResponse update(Long id, LeagueUpdate update);
    void delete(Long id);
}
