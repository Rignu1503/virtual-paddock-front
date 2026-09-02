package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.team.*;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.entities.Team;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.domain.repositories.TeamRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ITeamService;
import com.virtual_paddock.backend.infrastructure.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements ITeamService {

    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final TeamMapper teamMapper;

    @Override
    public TeamResponse create(TeamRequest request) {
        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new EntityNotFoundException("Liga no encontrada con ID: " + request.getLeagueId()));
        Team team = teamMapper.toEntity(request);
        team.setLeague(league);
        Team saved = teamRepository.save(team);
        return teamMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipo no encontrado con ID: " + id));
        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamBasicResponse> getAll(int page, int size) {
        Page<Team> teamPage = teamRepository.findAll(PageRequest.of(page, size));
        return buildPageResponse(teamPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamBasicResponse> getByLeagueId(Long leagueId, int page, int size) {
        Page<Team> teamPage = teamRepository.findByLeagueId(leagueId, PageRequest.of(page, size));
        return buildPageResponse(teamPage);
    }

    @Override
    public TeamResponse update(Long id, TeamUpdate update) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipo no encontrado con ID: " + id));
        teamMapper.updateEntityFromDto(update, team);
        Team updated = teamRepository.save(team);
        return teamMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new EntityNotFoundException("Equipo no encontrado con ID: " + id);
        }
        teamRepository.deleteById(id);
    }

    private PageResponse<TeamBasicResponse> buildPageResponse(Page<Team> page) {
        return PageResponse.<TeamBasicResponse>builder()
                .content(page.getContent().stream().map(teamMapper::toBasicResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
