package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.sanction.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

import java.util.UUID;

public interface ISanctionService {
    SanctionResponse create(SanctionRequest request);
    SanctionResponse getById(UUID id);
    PageResponse<SanctionBasicResponse> getAll(int page, int size);
    PageResponse<SanctionBasicResponse> getByRaceEventId(UUID raceEventId, int page, int size);
    PageResponse<SanctionBasicResponse> getByDriverId(UUID driverId, int page, int size);
    SanctionResponse update(UUID id, SanctionUpdate update);
    void delete(UUID id);
}
