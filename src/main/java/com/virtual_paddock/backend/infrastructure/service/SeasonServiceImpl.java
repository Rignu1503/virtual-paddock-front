package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.season.*;
import com.virtual_paddock.backend.domain.entities.Championship;
import com.virtual_paddock.backend.domain.entities.Season;
import com.virtual_paddock.backend.domain.repositories.ChampionshipRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISeasonService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.SeasonMapper;
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
public class SeasonServiceImpl implements ISeasonService {

    private final SeasonRepository seasonRepository;
    private final ChampionshipRepository championshipRepository;
    private final SeasonMapper seasonMapper;

    private Season find(UUID id) {
        return this.seasonRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Season")));
    }

    @Override
    public SeasonResponse create(SeasonRequest request) {
        Championship championship = championshipRepository.findById(request.getChampionshipId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Championship")));
        Season season = seasonMapper.toEntity(request);
        season.setChampionship(championship);
        Season saved = seasonRepository.save(season);
        return seasonMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonResponse getById(UUID id) {
        Season season = find(id);
        return seasonMapper.toResponse(season);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SeasonBasicResponse> getAll(int page, int size) {
        Page<Season> seasonPage = seasonRepository.findAll(PageRequest.of(page, size));
        return PageResponseHelper.fromPage(seasonPage, seasonMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SeasonBasicResponse> getByChampionshipId(UUID championshipId, int page, int size) {
        Page<Season> seasonPage = seasonRepository.findByChampionshipId(championshipId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(seasonPage, seasonMapper::toBasicResponse);
    }

    @Override
    public SeasonResponse update(UUID id, SeasonUpdate update) {
        Season season = find(id);
        seasonMapper.updateEntityFromDto(update, season);
        Season updated = seasonRepository.save(season);
        return seasonMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Season season = find(id);
        seasonRepository.delete(season);
    }
}
