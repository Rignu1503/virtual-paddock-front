package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimRequest;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimResponse;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimReviewRequest;
import com.virtual_paddock.backend.utils.enums.IncidentClaimStatus;

import java.util.List;
import java.util.UUID;

public interface IIncidentClaimService {
    IncidentClaimResponse create(IncidentClaimRequest request);
    IncidentClaimResponse getById(UUID id);
    PageResponse<IncidentClaimResponse> getAll(int page, int size, UUID raceEventId, IncidentClaimStatus status);
    List<IncidentClaimResponse> getAllByRaceEvent(UUID raceEventId);
    IncidentClaimResponse review(UUID id, IncidentClaimReviewRequest review);
    void delete(UUID id);
}
