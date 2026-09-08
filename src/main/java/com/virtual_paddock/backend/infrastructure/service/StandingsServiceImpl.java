package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.standings.DriverStandingResponse;
import com.virtual_paddock.backend.api.dtos.standings.TeamStandingResponse;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.Season;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IStandingsService;
import com.virtual_paddock.backend.infrastructure.helper.StandingsCalculatorHelper;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StandingsServiceImpl implements IStandingsService {

    private final SeasonRepository seasonRepository;
    private final RaceEventRepository raceEventRepository;
    private final StandingsCalculatorHelper standingsCalculatorHelper;

    private Season find(UUID id) {
        return this.seasonRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Season")));
    }

    @Override
    @Cacheable(value = "driverStandings", key = "#seasonId.toString() + '_' + (#category != null ? #category : 'ALL')")
    public List<DriverStandingResponse> getDriverStandings(UUID seasonId, String category) {
        Season season = find(seasonId);
        List<RaceEvent> raceEvents = raceEventRepository.findWithDetailsBySeasonId(season.getId());
        return standingsCalculatorHelper.calculateDriverStandings(raceEvents, category);
    }

    @Override
    @Cacheable(value = "teamStandings", key = "#seasonId.toString() + '_' + (#category != null ? #category : 'ALL')")
    public List<TeamStandingResponse> getTeamStandings(UUID seasonId, String category) {
        List<DriverStandingResponse> driverStandings = getDriverStandings(seasonId, category);
        return standingsCalculatorHelper.calculateTeamStandings(driverStandings);
    }
}
