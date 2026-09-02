package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.championship.*;
import com.virtual_paddock.backend.domain.entities.Championship;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.repositories.ChampionshipRepository;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IChampionshipService;
import com.virtual_paddock.backend.infrastructure.mapper.ChampionshipMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChampionshipServiceImpl implements IChampionshipService {

    private final ChampionshipRepository championshipRepository;
    private final LeagueRepository leagueRepository;
    private final com.virtual_paddock.backend.domain.repositories.PointsSystemRepository pointsSystemRepository;
    private final ChampionshipMapper championshipMapper;

    @Override
    public ChampionshipResponse create(ChampionshipRequest request) {
        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new EntityNotFoundException("Liga no encontrada con ID: " + request.getLeagueId()));
        Championship championship = championshipMapper.toEntity(request);
        championship.setLeague(league);

        if (request.getPointsSystemId() != null) {
            championship.setPointsSystemRef(pointsSystemRepository.findById(request.getPointsSystemId())
                    .orElseThrow(() -> new EntityNotFoundException("Sistema de puntos no encontrado con ID: " + request.getPointsSystemId())));
        }
        if (request.getSprintPointsSystemId() != null) {
            championship.setSprintPointsSystemRef(pointsSystemRepository.findById(request.getSprintPointsSystemId())
                    .orElseThrow(() -> new EntityNotFoundException("Sistema de puntos sprint no encontrado con ID: " + request.getSprintPointsSystemId())));
        }

        Championship saved = championshipRepository.save(championship);
        return championshipMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ChampionshipResponse getById(Long id) {
        Championship championship = championshipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campeonato no encontrado con ID: " + id));
        return championshipMapper.toResponse(championship);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChampionshipBasicResponse> getAll(int page, int size) {
        Page<Championship> championshipPage = championshipRepository.findAll(PageRequest.of(page, size));
        return buildPageResponse(championshipPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChampionshipBasicResponse> getByLeagueId(Long leagueId, int page, int size) {
        Page<Championship> championshipPage = championshipRepository.findByLeagueId(leagueId, PageRequest.of(page, size));
        return buildPageResponse(championshipPage);
    }

    @Override
    public ChampionshipResponse update(Long id, ChampionshipUpdate update) {
        Championship championship = championshipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campeonato no encontrado con ID: " + id));
        championshipMapper.updateEntityFromDto(update, championship);

        if (update.getPointsSystemId() != null) {
            championship.setPointsSystemRef(pointsSystemRepository.findById(update.getPointsSystemId())
                    .orElseThrow(() -> new EntityNotFoundException("Sistema de puntos no encontrado con ID: " + update.getPointsSystemId())));
        }
        if (update.getSprintPointsSystemId() != null) {
            championship.setSprintPointsSystemRef(pointsSystemRepository.findById(update.getSprintPointsSystemId())
                    .orElseThrow(() -> new EntityNotFoundException("Sistema de puntos sprint no encontrado con ID: " + update.getSprintPointsSystemId())));
        }

        Championship updated = championshipRepository.save(championship);
        return championshipMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!championshipRepository.existsById(id)) {
            throw new EntityNotFoundException("Campeonato no encontrado con ID: " + id);
        }
        championshipRepository.deleteById(id);
    }

    private PageResponse<ChampionshipBasicResponse> buildPageResponse(Page<Championship> page) {
        return PageResponse.<ChampionshipBasicResponse>builder()
                .content(page.getContent().stream().map(championshipMapper::toBasicResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
