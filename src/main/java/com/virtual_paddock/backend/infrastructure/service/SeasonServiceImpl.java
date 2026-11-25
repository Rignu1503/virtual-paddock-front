package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.season.*;
import com.virtual_paddock.backend.api.dtos.team.TeamResponse;
import com.virtual_paddock.backend.domain.entities.Championship;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.Season;
import com.virtual_paddock.backend.domain.entities.Team;
import com.virtual_paddock.backend.domain.repositories.ChampionshipRepository;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.domain.repositories.TeamRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISeasonService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.SeasonMapper;
import com.virtual_paddock.backend.infrastructure.mapper.TeamMapper;
import com.virtual_paddock.backend.infrastructure.security.LeagueSecurityValidator;
import com.virtual_paddock.backend.utils.enums.SeasonStatus;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SeasonServiceImpl implements ISeasonService {

    private final SeasonRepository seasonRepository;
    private final ChampionshipRepository championshipRepository;
    private final TeamRepository teamRepository;
    private final RaceEventRepository raceEventRepository;
    private final DriverRepository driverRepository;
    private final SeasonMapper seasonMapper;
    private final TeamMapper teamMapper;
    private final LeagueSecurityValidator leagueSecurityValidator;

    private void verifyOwnership(Championship championship) {
        if (championship != null && championship.getLeague() != null && leagueSecurityValidator != null) {
            leagueSecurityValidator.verifyOwnershipOrSuperadmin(championship.getLeague());
        }
    }

    private void verifyOwnership(Season season) {
        if (season != null && season.getChampionship() != null) {
            verifyOwnership(season.getChampionship());
        }
    }

    private Season find(UUID id) {
        return this.seasonRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Season")));
    }

    private TeamResponse mapTeamToResponse(Team team) {
        TeamResponse resp = teamMapper.toResponse(team);
        int max = (team.getMaxDrivers() != null && team.getMaxDrivers() > 0)
                ? team.getMaxDrivers()
                : (team.getChampionship() != null && team.getChampionship().getMaxDriversPerTeam() != null
                    ? team.getChampionship().getMaxDriversPerTeam() : 2);
        resp.setMaxDrivers(max);
        int count = (int) driverRepository.countByTeamId(team.getId());
        resp.setCurrentDriversCount(count);
        resp.setIsFull(count >= max);
        return resp;
    }

    @Override
    public SeasonResponse create(SeasonRequest request) {
        Championship championship = championshipRepository.findById(request.getChampionshipId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Championship")));
        verifyOwnership(championship);
        Season season = seasonMapper.toEntity(request);
        season.setChampionship(championship);

        // Fallback al category del campeonato si categories está vacío o nulo
        if (season.getCategories() == null || season.getCategories().trim().isEmpty()) {
            season.setCategories(championship.getCategory());
        }

        if (season.getStatus() == null) {
            season.setStatus(request.getStatus() != null ? request.getStatus() : SeasonStatus.ACTIVE);
        }

        if (Boolean.TRUE.equals(season.getIsDefault())) {
            seasonRepository.clearAllDefaults(championship.getId());
        } else if (season.getIsDefault() == null) {
            season.setIsDefault(false);
        }

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
        verifyOwnership(season);
        seasonMapper.updateEntityFromDto(update, season);

        if (update.getCategories() != null) {
            season.setCategories(update.getCategories());
        }

        if (update.getStatus() != null) {
            season.setStatus(update.getStatus());
        }

        if (Boolean.TRUE.equals(update.getIsDefault())) {
            seasonRepository.clearDefaultExcept(season.getChampionship().getId(), season.getId());
            season.setIsDefault(true);
        } else if (update.getIsDefault() != null) {
            season.setIsDefault(update.getIsDefault());
        }

        Season updated = seasonRepository.save(season);
        return seasonMapper.toResponse(updated);
    }

    @Override
    public SeasonResponse setDefaultSeason(UUID seasonId) {
        Season season = find(seasonId);
        verifyOwnership(season);
        UUID championshipId = season.getChampionship().getId();
        seasonRepository.clearDefaultExcept(championshipId, seasonId);
        season.setIsDefault(true);
        Season updated = seasonRepository.save(season);
        return seasonMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsBySeasonId(UUID seasonId) {
        find(seasonId);
        List<Team> teams = teamRepository.findBySeasonId(seasonId);
        return teams.stream().map(this::mapTeamToResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        Season season = find(id);
        verifyOwnership(season);

        // 1. Desvincular equipos de esta temporada para evitar violacion de clave foranea
        teamRepository.clearSeasonReference(id);

        // 2. Eliminar todas las rondas / eventos de carrera (cascada a resultados y sanciones)
        List<RaceEvent> events = raceEventRepository.findBySeasonId(id);
        if (!events.isEmpty()) {
            raceEventRepository.deleteAll(events);
        }

        // 3. Eliminar la temporada
        seasonRepository.delete(season);
    }
}
