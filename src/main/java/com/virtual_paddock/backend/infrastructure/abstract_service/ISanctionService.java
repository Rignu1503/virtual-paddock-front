package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.sanction.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

public interface ISanctionService {
    SanctionResponse create(SanctionRequest request);
    SanctionResponse getById(Long id);
    PageResponse<SanctionBasicResponse> getAll(int page, int size);
    PageResponse<SanctionBasicResponse> getByRaceEventId(Long raceEventId, int page, int size);
    PageResponse<SanctionBasicResponse> getByDriverId(Long driverId, int page, int size);
    SanctionResponse update(Long id, SanctionUpdate update);
    void delete(Long id);
}
