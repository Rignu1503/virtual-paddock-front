package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.raceevent.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

import java.util.UUID;

public interface IRaceEventService {
    RaceEventResponse create(RaceEventRequest request);
    RaceEventResponse getById(UUID id);
    PageResponse<RaceEventBasicResponse> getAll(int page, int size);
    PageResponse<RaceEventBasicResponse> getBySeasonId(UUID seasonId, int page, int size);
    RaceEventResponse update(UUID id, RaceEventUpdate update);
    void delete(UUID id);
}
