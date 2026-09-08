package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.raceevent.*;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.Season;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceEventService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.RaceEventMapper;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RaceEventServiceImpl implements IRaceEventService {

    private final RaceEventRepository raceEventRepository;
    private final SeasonRepository seasonRepository;
    private final RaceEventMapper raceEventMapper;

    private RaceEvent find(UUID id) {
        return this.raceEventRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));
    }

    @Override
    public RaceEventResponse create(RaceEventRequest request) {
        Season season = seasonRepository.findById(request.getSeasonId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Season")));
        RaceEvent raceEvent = raceEventMapper.toEntity(request);
        raceEvent.setSeason(season);
        RaceEvent saved = raceEventRepository.save(raceEvent);
        return raceEventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RaceEventResponse getById(UUID id) {
        RaceEvent raceEvent = find(id);
        return raceEventMapper.toResponse(raceEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RaceEventBasicResponse> getAll(int page, int size) {
        Page<RaceEvent> raceEventPage = raceEventRepository.findAll(PageRequest.of(page, size));
        return PageResponseHelper.fromPage(raceEventPage, raceEventMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RaceEventBasicResponse> getBySeasonId(UUID seasonId, int page, int size) {
        Page<RaceEvent> raceEventPage = raceEventRepository.findBySeasonId(seasonId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(raceEventPage, raceEventMapper::toBasicResponse);
    }

    @Override
    public RaceEventResponse update(UUID id, RaceEventUpdate update) {
        RaceEvent raceEvent = find(id);
        raceEventMapper.updateEntityFromDto(update, raceEvent);
        RaceEvent updated = raceEventRepository.save(raceEvent);
        return raceEventMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        RaceEvent raceEvent = find(id);
        raceEventRepository.delete(raceEvent);
    }
}
