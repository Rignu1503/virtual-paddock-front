package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.league.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

import java.util.UUID;

public interface ILeagueService {
    LeagueResponse create(LeagueRequest request);
    LeagueResponse getById(UUID id);
    LeagueResponse getBySlugUrl(String slugUrl);
    PageResponse<LeagueBasicResponse> getAll(int page, int size);
    LeagueResponse update(UUID id, LeagueUpdate update);
    void delete(UUID id);
}
