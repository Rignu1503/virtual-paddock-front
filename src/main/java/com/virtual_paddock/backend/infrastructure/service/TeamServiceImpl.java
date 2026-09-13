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
    private final com.virtual_paddock.backend.domain.repositories.ChampionshipRepository championshipRepository;
    private final com.virtual_paddock.backend.domain.repositories.SeasonRepository seasonRepository;
    private final TeamMapper teamMapper;

    private Team find(UUID id) {
        return this.teamRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Team")));
    }

    @Override
    public TeamResponse create(TeamRequest request) {
        Team team = teamMapper.toEntity(request);

        if (request.getChampionshipId() != null) {
            com.virtual_paddock.backend.domain.entities.Championship champ = championshipRepository.findById(request.getChampionshipId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Championship")));
            team.setChampionship(champ);
            team.setLeague(champ.getLeague());
        }

        if (request.getSeasonId() != null) {
            com.virtual_paddock.backend.domain.entities.Season season = seasonRepository.findById(request.getSeasonId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Season")));
            team.setSeason(season);
            if (team.getChampionship() == null) {
                team.setChampionship(season.getChampionship());
                team.setLeague(season.getChampionship().getLeague());
            }
        }

        if (team.getLeague() == null && request.getLeagueId() != null) {
            League league = leagueRepository.findById(request.getLeagueId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("League")));
            team.setLeague(league);
        }

        if (team.getLeague() == null && team.getChampionship() == null) {
            throw new BadRequestException("Debe especificar una liga o un campeonato para el equipo");
        }

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
    @Transactional(readOnly = true)
    public PageResponse<TeamBasicResponse> getByChampionshipId(UUID championshipId, int page, int size) {
        Page<Team> teamPage = teamRepository.findByChampionshipId(championshipId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(teamPage, teamMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamBasicResponse> getBySeasonId(UUID seasonId, int page, int size) {
        Page<Team> teamPage = teamRepository.findBySeasonId(seasonId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(teamPage, teamMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamBasicResponse> getFiltered(UUID leagueId, UUID championshipId, UUID seasonId, int page, int size) {
        Page<Team> teamPage = teamRepository.findByFilter(leagueId, championshipId, seasonId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(teamPage, teamMapper::toBasicResponse);
    }

    @Override
    public TeamResponse update(UUID id, TeamUpdate update) {
        Team team = find(id);
        teamMapper.updateEntityFromDto(update, team);

        if (update.getChampionshipId() != null) {
            com.virtual_paddock.backend.domain.entities.Championship champ = championshipRepository.findById(update.getChampionshipId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Championship")));
            team.setChampionship(champ);
            team.setLeague(champ.getLeague());
        }

        if (update.getSeasonId() != null) {
            com.virtual_paddock.backend.domain.entities.Season season = seasonRepository.findById(update.getSeasonId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Season")));
            team.setSeason(season);
            if (team.getChampionship() == null) {
                team.setChampionship(season.getChampionship());
                team.setLeague(season.getChampionship().getLeague());
            }
        }

        if (update.getLeagueId() != null && update.getChampionshipId() == null) {
            League league = leagueRepository.findById(update.getLeagueId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("League")));
            team.setLeague(league);
        }

        Team updated = teamRepository.save(team);
        return teamMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Team team = find(id);
        teamRepository.delete(team);
    }
}
