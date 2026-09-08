package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.raceresult.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

import java.util.List;
import java.util.UUID;

public interface IRaceResultService {
    RaceResultResponse create(RaceResultRequest request);
    RaceResultResponse getById(UUID id);
    PageResponse<RaceResultBasicResponse> getByRaceEventId(UUID raceEventId, int page, int size);
    PageResponse<RaceResultBasicResponse> getByDriverId(UUID driverId, int page, int size);
    RaceResultResponse update(UUID id, RaceResultUpdate update);
    void delete(UUID id);
    List<RaceResultResponse> processBatchResults(UUID raceEventId, RaceResultBulkRequest request);
    List<RaceResultResponse> previewBatchResults(UUID raceEventId, RaceResultBulkRequest request);
    List<RaceResultResponse> recalculateEventStandings(UUID raceEventId);
}
