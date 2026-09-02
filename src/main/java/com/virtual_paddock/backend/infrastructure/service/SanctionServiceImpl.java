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
import com.virtual_paddock.backend.infrastructure.abstract_service.ISanctionService;
import com.virtual_paddock.backend.infrastructure.mapper.SanctionMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SanctionServiceImpl implements ISanctionService {

    private final SanctionRepository sanctionRepository;
    private final RaceEventRepository raceEventRepository;
    private final DriverRepository driverRepository;
    private final RaceResultRepository raceResultRepository;
    private final SanctionMapper sanctionMapper;
    private final com.virtual_paddock.backend.infrastructure.abstract_service.IRaceResultService raceResultService;

    @Override
    public SanctionResponse create(SanctionRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(request.getRaceEventId())
                .orElseThrow(() -> new EntityNotFoundException("Evento de carrera no encontrado con ID: " + request.getRaceEventId()));
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new EntityNotFoundException("Piloto no encontrado con ID: " + request.getDriverId()));

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
    public SanctionResponse getById(Long id) {
        Sanction sanction = sanctionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sanción no encontrada con ID: " + id));
        return sanctionMapper.toResponse(sanction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanctionBasicResponse> getAll(int page, int size) {
        Page<Sanction> sanctionPage = sanctionRepository.findAll(PageRequest.of(page, size));
        return buildPageResponse(sanctionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanctionBasicResponse> getByRaceEventId(Long raceEventId, int page, int size) {
        Page<Sanction> sanctionPage = sanctionRepository.findByRaceEventId(raceEventId, PageRequest.of(page, size));
        return buildPageResponse(sanctionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanctionBasicResponse> getByDriverId(Long driverId, int page, int size) {
        Page<Sanction> sanctionPage = sanctionRepository.findByDriverId(driverId, PageRequest.of(page, size));
        return buildPageResponse(sanctionPage);
    }

    @Override
    public SanctionResponse update(Long id, SanctionUpdate update) {
        Sanction sanction = sanctionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sanción no encontrada con ID: " + id));
        sanctionMapper.updateEntityFromDto(update, sanction);
        Sanction updated = sanctionRepository.save(sanction);
        return sanctionMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!sanctionRepository.existsById(id)) {
            throw new EntityNotFoundException("Sanción no encontrada con ID: " + id);
        }
        sanctionRepository.deleteById(id);
    }

    private PageResponse<SanctionBasicResponse> buildPageResponse(Page<Sanction> page) {
        return PageResponse.<SanctionBasicResponse>builder()
                .content(page.getContent().stream().map(sanctionMapper::toBasicResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
