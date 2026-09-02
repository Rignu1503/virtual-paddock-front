package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.standings.DriverStandingResponse;
import com.virtual_paddock.backend.api.dtos.standings.TeamStandingResponse;
import com.virtual_paddock.backend.domain.entities.*;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SanctionRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.utils.enums.DriverStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandingsServiceImplTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private RaceEventRepository raceEventRepository;

    @Mock
    private SanctionRepository sanctionRepository;

    @InjectMocks
    private StandingsServiceImpl standingsService;

    private Season season;
    private RaceEvent raceEvent;
    private Driver driver1;
    private Driver driver2;
    private Team team1;

    @BeforeEach
    void setUp() {
        season = new Season();
        season.setId(1L);
        season.setSeasonName("Temporada 2026");

        team1 = new Team();
        team1.setId(10L);
        team1.setName("Red Bull Racing");

        driver1 = new Driver();
        driver1.setId(1L);
        driver1.setName("Max Verstappen");
        driver1.setGamertag("MaxV");
        driver1.setCarNumber("1");
        driver1.setCarModel("RB20");
        driver1.setTeam(team1);
        driver1.setStatus(DriverStatus.ACTIVE);

        driver2 = new Driver();
        driver2.setId(2L);
        driver2.setName("Sergio Perez");
        driver2.setGamertag("Checo");
        driver2.setCarNumber("11");
        driver2.setCarModel("RB20");
        driver2.setTeam(team1);
        driver2.setStatus(DriverStatus.ACTIVE);

        raceEvent = new RaceEvent();
        raceEvent.setId(100L);
        raceEvent.setSeason(season);
        raceEvent.setRaceResults(new ArrayList<>());
        raceEvent.setSanctions(new ArrayList<>());
    }

    @Test
    void getDriverStandings_success_withTieBreakerAndSanction() {
        // Driver 1: Posición 1, 25 puntos, Vuelta rápida
        RaceResult result1 = new RaceResult();
        result1.setId(1L);
        result1.setDriver(driver1);
        result1.setPosition(1);
        result1.setPoints(25);
        result1.setFastestLap(true);
        result1.setCategory("PRO");
        result1.setRaceEvent(raceEvent);

        // Driver 2: Posición 2, 18 puntos
        RaceResult result2 = new RaceResult();
        result2.setId(2L);
        result2.setDriver(driver2);
        result2.setPosition(2);
        result2.setPoints(18);
        result2.setFastestLap(false);
        result2.setCategory("PRO");
        result2.setRaceEvent(raceEvent);

        // Sanción a Driver 1: -5 puntos
        Sanction sanction = new Sanction();
        sanction.setId(1L);
        sanction.setDriver(driver1);
        sanction.setRaceEvent(raceEvent);
        sanction.setPointsDeduction(5);

        raceEvent.setRaceResults(List.of(result1, result2));
        raceEvent.setSanctions(List.of(sanction));

        when(seasonRepository.findById(1L)).thenReturn(Optional.of(season));
        when(raceEventRepository.findBySeasonId(1L)).thenReturn(List.of(raceEvent));

        // Act
        List<DriverStandingResponse> standings = standingsService.getDriverStandings(1L, null);

        // Assert: Driver 1 tiene 25 - 5 = 20 puntos, Driver 2 tiene 18 puntos
        assertNotNull(standings);
        assertEquals(2, standings.size());

        DriverStandingResponse first = standings.get(0);
        assertEquals(1L, first.getDriverId());
        assertEquals("Max Verstappen", first.getDriverName());
        assertEquals(20, first.getPoints());
        assertEquals(1, first.getWins());
        assertEquals(1, first.getPodiums());
        assertEquals(1, first.getFastestLaps());
        assertEquals(1, first.getPosition());

        DriverStandingResponse second = standings.get(1);
        assertEquals(2L, second.getDriverId());
        assertEquals("Sergio Perez", second.getDriverName());
        assertEquals(18, second.getPoints());
        assertEquals(0, second.getWins());
        assertEquals(1, second.getPodiums());
        assertEquals(2, second.getPosition());
    }

    @Test
    void getDriverStandings_filterByCategory() {
        RaceResult result1 = new RaceResult();
        result1.setId(1L);
        result1.setDriver(driver1);
        result1.setPosition(1);
        result1.setPoints(25);
        result1.setCategory("PRO");

        RaceResult result2 = new RaceResult();
        result2.setId(2L);
        result2.setDriver(driver2);
        result2.setPosition(1);
        result2.setPoints(25);
        result2.setCategory("AM");

        raceEvent.setRaceResults(List.of(result1, result2));

        when(seasonRepository.findById(1L)).thenReturn(Optional.of(season));
        when(raceEventRepository.findBySeasonId(1L)).thenReturn(List.of(raceEvent));

        // Act: filtrar solo "PRO"
        List<DriverStandingResponse> proStandings = standingsService.getDriverStandings(1L, "PRO");

        // Assert
        assertEquals(1, proStandings.size());
        assertEquals("Max Verstappen", proStandings.get(0).getDriverName());
    }

    @Test
    void getTeamStandings_success() {
        RaceResult result1 = new RaceResult();
        result1.setId(1L);
        result1.setDriver(driver1);
        result1.setPosition(1);
        result1.setPoints(25);
        result1.setCategory("PRO");

        RaceResult result2 = new RaceResult();
        result2.setId(2L);
        result2.setDriver(driver2);
        result2.setPosition(2);
        result2.setPoints(18);
        result2.setCategory("PRO");

        raceEvent.setRaceResults(List.of(result1, result2));

        when(seasonRepository.findById(1L)).thenReturn(Optional.of(season));
        when(raceEventRepository.findBySeasonId(1L)).thenReturn(List.of(raceEvent));

        // Act
        List<TeamStandingResponse> teamStandings = standingsService.getTeamStandings(1L, null);

        // Assert: Ambos pilotos pertenecen a team1 (Red Bull Racing) -> 25 + 18 = 43 puntos
        assertEquals(1, teamStandings.size());
        TeamStandingResponse teamResponse = teamStandings.get(0);
        assertEquals(10L, teamResponse.getTeamId());
        assertEquals("Red Bull Racing", teamResponse.getTeamName());
        assertEquals(43, teamResponse.getPoints());
        assertEquals(1, teamResponse.getWins());
        assertEquals(2, teamResponse.getPodiums());
        assertEquals(1, teamResponse.getPosition());
    }

    @Test
    void getDriverStandings_seasonNotFound_throwsException() {
        when(seasonRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> standingsService.getDriverStandings(99L, null));
    }
}
