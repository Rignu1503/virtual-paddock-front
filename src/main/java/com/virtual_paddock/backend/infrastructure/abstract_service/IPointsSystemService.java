package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.pointssystem.*;

import java.util.List;

public interface IPointsSystemService {
    PointsSystemResponse create(PointsSystemRequest request);
    PointsSystemResponse getById(Long id);
    List<PointsSystemResponse> getByLeagueId(Long leagueId);
    PageResponse<PointsSystemBasicResponse> getByLeagueIdPaged(Long leagueId, int page, int size);
    PointsSystemResponse update(Long id, PointsSystemUpdate update);
    void delete(Long id);
}
