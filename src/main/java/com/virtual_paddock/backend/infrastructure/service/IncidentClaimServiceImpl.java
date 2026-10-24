package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimRequest;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimResponse;
import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimReviewRequest;
import com.virtual_paddock.backend.api.dtos.sanction.SanctionRequest;
import com.virtual_paddock.backend.api.dtos.sanction.SanctionResponse;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.IncidentClaim;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.Sanction;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.IncidentClaimRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SanctionRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IIncidentClaimService;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISanctionService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.IncidentClaimMapper;
import com.virtual_paddock.backend.utils.enums.*;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentClaimServiceImpl implements IIncidentClaimService {

    private final IncidentClaimRepository incidentClaimRepository;
    private final RaceEventRepository raceEventRepository;
    private final DriverRepository driverRepository;
    private final ISanctionService sanctionService;
    private final SanctionRepository sanctionRepository;
    private final IncidentClaimMapper incidentClaimMapper;

    private IncidentClaim find(UUID id) {
        return incidentClaimRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("IncidentClaim")));
    }

    @Override
    public IncidentClaimResponse create(IncidentClaimRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(request.getRaceEventId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));

        Driver reportedDriver = null;
        if (request.getReportedDriverId() != null) {
            reportedDriver = driverRepository.findById(request.getReportedDriverId()).orElse(null);
        }

        // Generar número de ticket consecutivo y único
        int year = LocalDateTime.now().getYear();
        long count = incidentClaimRepository.count() + 1;
        String ticketNumber = String.format("INC-%d-%04d", year, count);
        while (incidentClaimRepository.existsByTicketNumber(ticketNumber)) {
            count++;
            ticketNumber = String.format("INC-%d-%04d", year, count);
        }

        IncidentClaim claim = IncidentClaim.builder()
                .ticketNumber(ticketNumber)
                .raceEvent(raceEvent)
                .reportingDriverName(request.getReportingDriverName().trim())
                .reportingDriverSteamId(request.getReportingDriverSteamId() != null ? request.getReportingDriverSteamId().trim() : null)
                .reportedDriver(reportedDriver)
                .reportedDriverName(request.getReportedDriverName().trim())
                .lapOrMinute(request.getLapOrMinute() != null ? request.getLapOrMinute().trim() : null)
                .videoUrl(request.getVideoUrl() != null ? request.getVideoUrl().trim() : null)
                .incidentType(request.getIncidentType())
                .description(request.getDescription().trim())
                .status(IncidentClaimStatus.PENDIENTE)
                .penaltyType(IncidentPenaltyType.NINGUNA)
                .penaltySeconds(0)
                .pointsDeduction(0)
                .createdAt(LocalDateTime.now())
                .build();

        IncidentClaim saved = incidentClaimRepository.save(claim);
        return incidentClaimMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentClaimResponse getById(UUID id) {
        IncidentClaim claim = find(id);
        return incidentClaimMapper.toResponse(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<IncidentClaimResponse> getAll(int page, int size, UUID raceEventId, IncidentClaimStatus status) {
        Page<IncidentClaim> claimPage = incidentClaimRepository.findByFilter(raceEventId, status, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(claimPage, incidentClaimMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentClaimResponse> getAllByRaceEvent(UUID raceEventId) {
        List<IncidentClaim> claims = incidentClaimRepository.findByRaceEventIdOrderByCreatedAtDesc(raceEventId);
        return claims.stream().map(incidentClaimMapper::toResponse).toList();
    }

    @Override
    public IncidentClaimResponse review(UUID id, IncidentClaimReviewRequest review) {
        IncidentClaim claim = find(id);

        claim.setStatus(review.getStatus());
        claim.setStewardName(review.getStewardName() != null ? review.getStewardName().trim() : "Comisario Deportivo");
        claim.setStewardNotes(review.getStewardNotes() != null ? review.getStewardNotes().trim() : "");
        claim.setReviewedAt(LocalDateTime.now());

        IncidentPenaltyType pType = review.getPenaltyType() != null ? review.getPenaltyType() : IncidentPenaltyType.NINGUNA;
        int pSeconds = review.getPenaltySeconds() != null ? review.getPenaltySeconds() : 0;
        int pPoints = review.getPointsDeduction() != null ? review.getPointsDeduction() : 0;

        claim.setPenaltyType(pType);
        claim.setPenaltySeconds(pSeconds);
        claim.setPointsDeduction(pPoints);
        claim.setPenaltyDetail(review.getPenaltyDetail());

        // Si el estado es APLICADO y hay una sanción efectiva, sincronizar con la entidad Sanction oficial
        if (review.getStatus() == IncidentClaimStatus.APLICADO && pType != IncidentPenaltyType.NINGUNA) {
            Driver driver = claim.getReportedDriver();
            if (driver != null) {
                SanctionType sType = SanctionType.CONDUCTA;
                if (claim.getIncidentType() == IncidentClaimType.COLISION) {
                    sType = SanctionType.COLISION;
                } else if (claim.getIncidentType() == IncidentClaimType.TRACK_LIMITS) {
                    sType = SanctionType.TRACK_LIMITS;
                }

                SanctionSeverity severity = SanctionSeverity.LEVE;
                if (pType == IncidentPenaltyType.DSQ || pPoints >= 5) {
                    severity = SanctionSeverity.GRAVE;
                } else if (pSeconds >= 10 || pPoints > 0) {
                    severity = SanctionSeverity.MODERADO;
                }

                String detail = String.format("[%s] %s", claim.getTicketNumber(),
                        (review.getStewardNotes() != null && !review.getStewardNotes().isBlank())
                                ? review.getStewardNotes().trim()
                                : "Sanción aplicada por comisaría deportiva");

                if (claim.getSanction() == null) {
                    SanctionRequest sReq = SanctionRequest.builder()
                            .raceEventId(claim.getRaceEvent().getId())
                            .driverId(driver.getId())
                            .type(sType)
                            .severity(severity)
                            .status(SanctionStatus.APLICADO)
                            .penaltySeconds(pSeconds)
                            .pointsDeduction(pPoints)
                            .detail(detail)
                            .build();

                    SanctionResponse createdSanction = sanctionService.create(sReq);
                    sanctionRepository.findById(createdSanction.getId()).ifPresent(claim::setSanction);
                }
            }
        } else if (review.getStatus() != IncidentClaimStatus.APLICADO && claim.getSanction() != null) {
            // Si el veredicto cambia a DESESTIMADO o EN_REVISION, revertir la sanción oficial
            UUID sanctionId = claim.getSanction().getId();
            claim.setSanction(null);
            sanctionService.delete(sanctionId);
        }

        IncidentClaim saved = incidentClaimRepository.save(claim);
        return incidentClaimMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        IncidentClaim claim = find(id);
        if (claim.getSanction() != null) {
            UUID sanctionId = claim.getSanction().getId();
            claim.setSanction(null);
            sanctionService.delete(sanctionId);
        }
        incidentClaimRepository.delete(claim);
    }
}
