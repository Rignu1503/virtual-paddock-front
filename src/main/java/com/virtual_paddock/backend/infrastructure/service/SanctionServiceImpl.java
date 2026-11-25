package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.sanction.*;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.Sanction;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.RaceResultRepository;
import com.virtual_paddock.backend.domain.repositories.SanctionRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceResultService;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISanctionService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.SanctionMapper;
import com.virtual_paddock.backend.infrastructure.security.LeagueSecurityValidator;
import com.virtual_paddock.backend.utils.enums.SanctionSeverity;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SanctionServiceImpl implements ISanctionService {

    private final SanctionRepository sanctionRepository;
    private final RaceEventRepository raceEventRepository;
    private final DriverRepository driverRepository;
    private final RaceResultRepository raceResultRepository;
    private final SanctionMapper sanctionMapper;
    private final IRaceResultService raceResultService;
    private final LeagueSecurityValidator leagueSecurityValidator;

    private void verifyOwnership(RaceEvent raceEvent) {
        if (raceEvent != null && raceEvent.getSeason() != null
                && raceEvent.getSeason().getChampionship() != null
                && raceEvent.getSeason().getChampionship().getLeague() != null
                && leagueSecurityValidator != null) {
            leagueSecurityValidator.verifyOwnershipOrSuperadmin(raceEvent.getSeason().getChampionship().getLeague());
        }
    }

    private Sanction find(UUID id) {
        return this.sanctionRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Sanction")));
    }

    private boolean isDisqualification(Sanction sanction) {
        if (sanction.getSeverity() == SanctionSeverity.DESCALIFICACION
                || sanction.getSeverity() == SanctionSeverity.DISQUALIFICATION) {
            return true;
        }
        if (sanction.getPenaltySeconds() != null && sanction.getPenaltySeconds() >= 999) {
            return true;
        }
        if (sanction.getDetail() != null) {
            String detailUpper = sanction.getDetail().toUpperCase();
            if (detailUpper.contains("DSQ") || detailUpper.contains("DESCALIF") || detailUpper.contains("DISQUALIF")) {
                return true;
            }
        }
        return false;
    }

    private void applySanctionToRaceResult(UUID raceEventId, UUID driverId, Sanction sanction) {
        if (isDisqualification(sanction)) {
            raceResultRepository.findByRaceEventIdAndDriverId(raceEventId, driverId).ifPresent(rr -> {
                rr.setStatus("DQ");
                rr.setPoints(0);
                raceResultRepository.save(rr);
                raceResultService.recalculateEventStandings(raceEventId);
            });
        } else if (sanction.getPenaltySeconds() != null && sanction.getPenaltySeconds() > 0) {
            raceResultRepository.findByRaceEventIdAndDriverId(raceEventId, driverId).ifPresent(rr -> {
                int currentPenalties = rr.getPenaltiesSeconds() != null ? rr.getPenaltiesSeconds() : 0;
                rr.setPenaltiesSeconds(currentPenalties + sanction.getPenaltySeconds());
                raceResultRepository.save(rr);
                raceResultService.recalculateEventStandings(raceEventId);
            });
        }
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public SanctionResponse create(SanctionRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(request.getRaceEventId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));
        verifyOwnership(raceEvent);
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Driver")));

        Sanction sanction = sanctionMapper.toEntity(request);
        sanction.setRaceEvent(raceEvent);
        sanction.setDriver(driver);
        Sanction saved = sanctionRepository.save(sanction);

        applySanctionToRaceResult(raceEvent.getId(), driver.getId(), saved);

        return sanctionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SanctionResponse getById(UUID id) {
        Sanction sanction = find(id);
        return sanctionMapper.toResponse(sanction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanctionBasicResponse> getAll(int page, int size) {
        Page<Sanction> sanctionPage = sanctionRepository.findAll(PageRequest.of(page, size));
        return PageResponseHelper.fromPage(sanctionPage, sanctionMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanctionBasicResponse> getByRaceEventId(UUID raceEventId, int page, int size) {
        Page<Sanction> sanctionPage = sanctionRepository.findByRaceEventId(raceEventId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(sanctionPage, sanctionMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanctionBasicResponse> getByDriverId(UUID driverId, int page, int size) {
        Page<Sanction> sanctionPage = sanctionRepository.findByDriverId(driverId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(sanctionPage, sanctionMapper::toBasicResponse);
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public SanctionResponse update(UUID id, SanctionUpdate update) {
        Sanction sanction = find(id);
        verifyOwnership(sanction.getRaceEvent());
        sanctionMapper.updateEntityFromDto(update, sanction);
        Sanction updated = sanctionRepository.save(sanction);

        applySanctionToRaceResult(updated.getRaceEvent().getId(), updated.getDriver().getId(), updated);

        return sanctionMapper.toResponse(updated);
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public void delete(UUID id) {
        Sanction sanction = find(id);
        verifyOwnership(sanction.getRaceEvent());
        sanctionRepository.delete(sanction);
    }
}
