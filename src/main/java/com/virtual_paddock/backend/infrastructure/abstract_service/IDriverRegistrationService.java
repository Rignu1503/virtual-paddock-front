package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.driver.DriverResponse;
import com.virtual_paddock.backend.api.dtos.driver_registration.*;
import com.virtual_paddock.backend.utils.enums.DriverRegistrationStatus;

import java.util.UUID;

public interface IDriverRegistrationService {
    DriverRegistrationResponse register(DriverRegistrationRequest request);
    DriverRegistrationResponse getById(UUID id);
    PageResponse<DriverRegistrationResponse> getFiltered(UUID leagueId, UUID championshipId, DriverRegistrationStatus status, int page, int size);
    DriverResponse approve(UUID id, DriverApprovalRequest approval);
    DriverRegistrationResponse reject(UUID id, DriverRejectionRequest rejection);
    long countPending(UUID leagueId);
}
