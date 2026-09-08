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

    private Sanction find(UUID id) {
        return this.sanctionRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Sanction")));
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public SanctionResponse create(SanctionRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(request.getRaceEventId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Driver")));

        Sanction sanction = sanctionMapper.toEntity(request);
        sanction.setRaceEvent(raceEvent);
        sanction.setDriver(driver);
        Sanction saved = sanctionRepository.save(sanction);

        // Si la sanción tiene segundos de penalización y está APLICADA, actualizar resultado y recalcular carrera
        if (saved.getPenaltySeconds() != null && saved.getPenaltySeconds() > 0) {
            raceResultRepository.findByRaceEventIdAndDriverId(raceEvent.getId(), driver.getId()).ifPresent(rr -> {
                int currentPenalties = rr.getPenaltiesSeconds() != null ? rr.getPenaltiesSeconds() : 0;
                rr.setPenaltiesSeconds(currentPenalties + saved.getPenaltySeconds());
                raceResultRepository.save(rr);
                raceResultService.recalculateEventStandings(raceEvent.getId());
            });
        }

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
        sanctionMapper.updateEntityFromDto(update, sanction);
        Sanction updated = sanctionRepository.save(sanction);
        return sanctionMapper.toResponse(updated);
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public void delete(UUID id) {
        Sanction sanction = find(id);
        sanctionRepository.delete(sanction);
    }
}
