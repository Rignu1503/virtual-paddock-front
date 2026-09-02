package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.raceevent.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

public interface IRaceEventService {
    RaceEventResponse create(RaceEventRequest request);
    RaceEventResponse getById(Long id);
    PageResponse<RaceEventBasicResponse> getAll(int page, int size);
    PageResponse<RaceEventBasicResponse> getBySeasonId(Long seasonId, int page, int size);
    RaceEventResponse update(Long id, RaceEventUpdate update);
    void delete(Long id);
}
