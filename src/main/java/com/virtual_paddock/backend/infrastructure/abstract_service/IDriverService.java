package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.driver.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

public interface IDriverService {
    DriverResponse create(DriverRequest request);
    DriverResponse getById(Long id);
    PageResponse<DriverBasicResponse> getAll(int page, int size);
    PageResponse<DriverBasicResponse> getByTeamId(Long teamId, int page, int size);
    DriverResponse update(Long id, DriverUpdate update);
    void delete(Long id);
}
