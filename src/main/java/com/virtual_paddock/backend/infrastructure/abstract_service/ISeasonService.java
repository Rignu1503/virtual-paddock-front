package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.season.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

public interface ISeasonService {
    SeasonResponse create(SeasonRequest request);
    SeasonResponse getById(Long id);
    PageResponse<SeasonBasicResponse> getAll(int page, int size);
    PageResponse<SeasonBasicResponse> getByChampionshipId(Long championshipId, int page, int size);
    SeasonResponse update(Long id, SeasonUpdate update);
    void delete(Long id);
}
