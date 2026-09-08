package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.championship.*;
import com.virtual_paddock.backend.domain.entities.Championship;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.repositories.ChampionshipRepository;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.domain.repositories.PointsSystemRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IChampionshipService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.ChampionshipMapper;
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
public class ChampionshipServiceImpl implements IChampionshipService {

    private final ChampionshipRepository championshipRepository;
    private final LeagueRepository leagueRepository;
    private final PointsSystemRepository pointsSystemRepository;
    private final ChampionshipMapper championshipMapper;

    private Championship find(UUID id) {
        return this.championshipRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Championship")));
    }

    @Override
    public ChampionshipResponse create(ChampionshipRequest request) {
        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("League")));
        Championship championship = championshipMapper.toEntity(request);
        championship.setLeague(league);

        if (request.getPointsSystemId() != null) {
            championship.setPointsSystemRef(pointsSystemRepository.findById(request.getPointsSystemId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("PointsSystem"))));
        }
        if (request.getSprintPointsSystemId() != null) {
            championship.setSprintPointsSystemRef(pointsSystemRepository.findById(request.getSprintPointsSystemId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("SprintPointsSystem"))));
        }

        Championship saved = championshipRepository.save(championship);
        return championshipMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ChampionshipResponse getById(UUID id) {
        Championship championship = find(id);
        return championshipMapper.toResponse(championship);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChampionshipBasicResponse> getAll(int page, int size) {
        Page<Championship> championshipPage = championshipRepository.findAll(PageRequest.of(page, size));
        return PageResponseHelper.fromPage(championshipPage, championshipMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChampionshipBasicResponse> getByLeagueId(UUID leagueId, int page, int size) {
        Page<Championship> championshipPage = championshipRepository.findByLeagueId(leagueId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(championshipPage, championshipMapper::toBasicResponse);
    }

    @Override
    public ChampionshipResponse update(UUID id, ChampionshipUpdate update) {
        Championship championship = find(id);
        championshipMapper.updateEntityFromDto(update, championship);

        if (update.getPointsSystemId() != null) {
            championship.setPointsSystemRef(pointsSystemRepository.findById(update.getPointsSystemId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("PointsSystem"))));
        }
        if (update.getSprintPointsSystemId() != null) {
            championship.setSprintPointsSystemRef(pointsSystemRepository.findById(update.getSprintPointsSystemId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("SprintPointsSystem"))));
        }

        Championship updated = championshipRepository.save(championship);
        return championshipMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Championship championship = find(id);
        championshipRepository.delete(championship);
    }
}
