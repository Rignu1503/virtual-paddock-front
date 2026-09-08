package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.team.*;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.entities.Team;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.domain.repositories.TeamRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ITeamService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.TeamMapper;
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
public class TeamServiceImpl implements ITeamService {

    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final TeamMapper teamMapper;

    private Team find(UUID id) {
        return this.teamRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Team")));
    }

    @Override
    public TeamResponse create(TeamRequest request) {
        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("League")));
        Team team = teamMapper.toEntity(request);
        team.setLeague(league);
        Team saved = teamRepository.save(team);
        return teamMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(UUID id) {
        Team team = find(id);
        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamBasicResponse> getAll(int page, int size) {
        Page<Team> teamPage = teamRepository.findAll(PageRequest.of(page, size));
        return PageResponseHelper.fromPage(teamPage, teamMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamBasicResponse> getByLeagueId(UUID leagueId, int page, int size) {
        Page<Team> teamPage = teamRepository.findByLeagueId(leagueId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(teamPage, teamMapper::toBasicResponse);
    }

    @Override
    public TeamResponse update(UUID id, TeamUpdate update) {
        Team team = find(id);
        teamMapper.updateEntityFromDto(update, team);
        Team updated = teamRepository.save(team);
        return teamMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Team team = find(id);
        teamRepository.delete(team);
    }
}
