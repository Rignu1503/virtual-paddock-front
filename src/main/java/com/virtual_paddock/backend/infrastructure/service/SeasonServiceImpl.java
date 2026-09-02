package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.season.*;
import com.virtual_paddock.backend.domain.entities.Championship;
import com.virtual_paddock.backend.domain.entities.Season;
import com.virtual_paddock.backend.domain.repositories.ChampionshipRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISeasonService;
import com.virtual_paddock.backend.infrastructure.mapper.SeasonMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SeasonServiceImpl implements ISeasonService {

    private final SeasonRepository seasonRepository;
    private final ChampionshipRepository championshipRepository;
    private final SeasonMapper seasonMapper;

    @Override
    public SeasonResponse create(SeasonRequest request) {
        Championship championship = championshipRepository.findById(request.getChampionshipId())
                .orElseThrow(() -> new EntityNotFoundException("Campeonato no encontrado con ID: " + request.getChampionshipId()));
        Season season = seasonMapper.toEntity(request);
        season.setChampionship(championship);
        Season saved = seasonRepository.save(season);
        return seasonMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonResponse getById(Long id) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Temporada no encontrada con ID: " + id));
        return seasonMapper.toResponse(season);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SeasonBasicResponse> getAll(int page, int size) {
        Page<Season> seasonPage = seasonRepository.findAll(PageRequest.of(page, size));
        return buildPageResponse(seasonPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SeasonBasicResponse> getByChampionshipId(Long championshipId, int page, int size) {
        Page<Season> seasonPage = seasonRepository.findByChampionshipId(championshipId, PageRequest.of(page, size));
        return buildPageResponse(seasonPage);
    }

    @Override
    public SeasonResponse update(Long id, SeasonUpdate update) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Temporada no encontrada con ID: " + id));
        seasonMapper.updateEntityFromDto(update, season);
        Season updated = seasonRepository.save(season);
        return seasonMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!seasonRepository.existsById(id)) {
            throw new EntityNotFoundException("Temporada no encontrada con ID: " + id);
        }
        seasonRepository.deleteById(id);
    }

    private PageResponse<SeasonBasicResponse> buildPageResponse(Page<Season> page) {
        return PageResponse.<SeasonBasicResponse>builder()
                .content(page.getContent().stream().map(seasonMapper::toBasicResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
