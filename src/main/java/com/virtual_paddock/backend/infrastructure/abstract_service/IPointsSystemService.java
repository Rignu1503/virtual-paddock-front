package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.pointssystem.*;

import java.util.List;
import java.util.UUID;

public interface IPointsSystemService {
    PointsSystemResponse create(PointsSystemRequest request);
    PointsSystemResponse getById(UUID id);
    List<PointsSystemResponse> getByLeagueId(UUID leagueId);
    PageResponse<PointsSystemBasicResponse> getByLeagueIdPaged(UUID leagueId, int page, int size);
    PointsSystemResponse update(UUID id, PointsSystemUpdate update);
    void delete(UUID id);
}
