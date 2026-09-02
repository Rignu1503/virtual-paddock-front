package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.raceresult.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

public interface IRaceResultService {
    RaceResultResponse create(RaceResultRequest request);
    RaceResultResponse getById(Long id);
    PageResponse<RaceResultBasicResponse> getByRaceEventId(Long raceEventId, int page, int size);
    PageResponse<RaceResultBasicResponse> getByDriverId(Long driverId, int page, int size);
    RaceResultResponse update(Long id, RaceResultUpdate update);
    void delete(Long id);
    java.util.List<RaceResultResponse> processBatchResults(Long raceEventId, RaceResultBulkRequest request);
    java.util.List<RaceResultResponse> previewBatchResults(Long raceEventId, RaceResultBulkRequest request);
    java.util.List<RaceResultResponse> recalculateEventStandings(Long raceEventId);
}
