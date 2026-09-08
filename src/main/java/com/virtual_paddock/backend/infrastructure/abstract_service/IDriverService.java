package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.driver.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

import java.util.UUID;

public interface IDriverService {
    DriverResponse create(DriverRequest request);
    DriverResponse getById(UUID id);
    PageResponse<DriverBasicResponse> getAll(int page, int size);
    PageResponse<DriverBasicResponse> getByTeamId(UUID teamId, int page, int size);
    DriverResponse update(UUID id, DriverUpdate update);
    void delete(UUID id);
}
