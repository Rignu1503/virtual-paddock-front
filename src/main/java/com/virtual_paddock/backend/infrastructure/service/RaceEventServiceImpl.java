package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.raceevent.*;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.Season;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceEventService;
import com.virtual_paddock.backend.infrastructure.mapper.RaceEventMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RaceEventServiceImpl implements IRaceEventService {

    private final RaceEventRepository raceEventRepository;
    private final SeasonRepository seasonRepository;
    private final RaceEventMapper raceEventMapper;

    @Override
    public RaceEventResponse create(RaceEventRequest request) {
        Season season = seasonRepository.findById(request.getSeasonId())
                .orElseThrow(() -> new EntityNotFoundException("Temporada no encontrada con ID: " + request.getSeasonId()));
        RaceEvent raceEvent = raceEventMapper.toEntity(request);
        raceEvent.setSeason(season);
        RaceEvent saved = raceEventRepository.save(raceEvent);
        return raceEventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RaceEventResponse getById(Long id) {
        RaceEvent raceEvent = raceEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de carrera no encontrado con ID: " + id));
        return raceEventMapper.toResponse(raceEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RaceEventBasicResponse> getAll(int page, int size) {
        Page<RaceEvent> raceEventPage = raceEventRepository.findAll(PageRequest.of(page, size));
        return buildPageResponse(raceEventPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RaceEventBasicResponse> getBySeasonId(Long seasonId, int page, int size) {
        Page<RaceEvent> raceEventPage = raceEventRepository.findBySeasonId(seasonId, PageRequest.of(page, size));
        return buildPageResponse(raceEventPage);
    }

    @Override
    public RaceEventResponse update(Long id, RaceEventUpdate update) {
        RaceEvent raceEvent = raceEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de carrera no encontrado con ID: " + id));
        raceEventMapper.updateEntityFromDto(update, raceEvent);
        RaceEvent updated = raceEventRepository.save(raceEvent);
        return raceEventMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!raceEventRepository.existsById(id)) {
            throw new EntityNotFoundException("Evento de carrera no encontrado con ID: " + id);
        }
        raceEventRepository.deleteById(id);
    }

    private PageResponse<RaceEventBasicResponse> buildPageResponse(Page<RaceEvent> page) {
        return PageResponse.<RaceEventBasicResponse>builder()
                .content(page.getContent().stream().map(raceEventMapper::toBasicResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
