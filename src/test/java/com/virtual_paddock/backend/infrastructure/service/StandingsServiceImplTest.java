package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.standings.DriverStandingResponse;
import com.virtual_paddock.backend.api.dtos.standings.TeamStandingResponse;
import com.virtual_paddock.backend.domain.entities.*;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SanctionRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.infrastructure.helper.StandingsCalculatorHelper;
import com.virtual_paddock.backend.utils.enums.DriverStatus;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    @Spy
    private StandingsCalculatorHelper standingsCalculatorHelper = new StandingsCalculatorHelper();

    @InjectMocks
    private StandingsServiceImpl standingsService;

    private UUID seasonId;
    private UUID team1Id;
    private UUID driver1Id;
    private UUID driver2Id;
    private UUID raceEventId;

    private Season season;
    private RaceEvent raceEvent;
    private Driver driver1;
    private Driver driver2;
    private Team team1;

    @BeforeEach
    void setUp() {
        seasonId = UUID.randomUUID();
        team1Id = UUID.randomUUID();
        driver1Id = UUID.randomUUID();
        driver2Id = UUID.randomUUID();
        raceEventId = UUID.randomUUID();

        season = new Season();
        season.setId(seasonId);
        season.setSeasonName("Temporada 2026");

        team1 = new Team();
        team1.setId(team1Id);
        team1.setName("Red Bull Racing");

        driver1 = new Driver();
        driver1.setId(driver1Id);
        driver1.setName("Max Verstappen");
        driver1.setGamertag("MaxV");
        driver1.setCarNumber("1");
        driver1.setCarModel("RB20");
        driver1.setTeam(team1);
        driver1.setStatus(DriverStatus.ACTIVE);

        driver2 = new Driver();
        driver2.setId(driver2Id);
        driver2.setName("Sergio Perez");
        driver2.setGamertag("Checo");
        driver2.setCarNumber("11");
        driver2.setCarModel("RB20");
        driver2.setTeam(team1);
        driver2.setStatus(DriverStatus.ACTIVE);

        raceEvent = new RaceEvent();
        raceEvent.setId(raceEventId);
        raceEvent.setSeason(season);
        raceEvent.setDate(Instant.parse("2026-03-01T15:00:00Z"));
        raceEvent.setRoundNumber("R01");
        raceEvent.setRaceResults(new ArrayList<>());
        raceEvent.setSanctions(new HashSet<>());
    }

    @Test
    void getDriverStandings_success_withTieBreakerAndSanction() {
        // Driver 1: Posición 1, 25 puntos, Vuelta rápida
        RaceResult result1 = new RaceResult();
        result1.setId(UUID.randomUUID());
        result1.setDriver(driver1);
        result1.setPosition(1);
        result1.setPoints(25);
        result1.setFastestLap(true);
        result1.setCategory("PRO");
        result1.setRaceEvent(raceEvent);

        // Driver 2: Posición 2, 18 puntos
        RaceResult result2 = new RaceResult();
        result2.setId(UUID.randomUUID());
        result2.setDriver(driver2);
        result2.setPosition(2);
        result2.setPoints(18);
        result2.setFastestLap(false);
        result2.setCategory("PRO");
        result2.setRaceEvent(raceEvent);

        // Sanción a Driver 1: -5 puntos
        Sanction sanction = new Sanction();
        sanction.setId(UUID.randomUUID());
        sanction.setDriver(driver1);
        sanction.setRaceEvent(raceEvent);
        sanction.setPointsDeduction(5);

        raceEvent.setRaceResults(List.of(result1, result2));
        raceEvent.setSanctions(Set.of(sanction));

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(raceEventRepository.findWithDetailsBySeasonId(seasonId)).thenReturn(List.of(raceEvent));

        // Act
        List<DriverStandingResponse> standings = standingsService.getDriverStandings(seasonId, null);

        // Assert: Driver 1 tiene 25 - 5 = 20 puntos, Driver 2 tiene 18 puntos
        assertNotNull(standings);
        assertEquals(2, standings.size());

        DriverStandingResponse first = standings.get(0);
        assertEquals(driver1Id, first.getDriverId());
        assertEquals("Max Verstappen", first.getDriverName());
        assertEquals(20, first.getPoints());
        assertEquals(1, first.getWins());
        assertEquals(1, first.getPodiums());
        assertEquals(1, first.getFastestLaps());
        assertEquals(1, first.getPosition());
        assertEquals("-", first.getGap());
        assertNull(first.getPrevPosition()); // Solo 1 evento completado

        DriverStandingResponse second = standings.get(1);
        assertEquals(driver2Id, second.getDriverId());
        assertEquals("Sergio Perez", second.getDriverName());
        assertEquals(18, second.getPoints());
        assertEquals(0, second.getWins());
        assertEquals(1, second.getPodiums());
        assertEquals(2, second.getPosition());
        assertEquals("-2", second.getGap());
        assertNull(second.getPrevPosition()); // Solo 1 evento completado
    }

    @Test
    void getDriverStandings_filterByCategory() {
        RaceResult result1 = new RaceResult();
        result1.setId(UUID.randomUUID());
        result1.setDriver(driver1);
        result1.setPosition(1);
        result1.setPoints(25);
        result1.setCategory("PRO");

        RaceResult result2 = new RaceResult();
        result2.setId(UUID.randomUUID());
        result2.setDriver(driver2);
        result2.setPosition(1);
        result2.setPoints(25);
        result2.setCategory("AM");

        raceEvent.setRaceResults(List.of(result1, result2));

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(raceEventRepository.findWithDetailsBySeasonId(seasonId)).thenReturn(List.of(raceEvent));

        // Act: filtrar solo "PRO"
        List<DriverStandingResponse> proStandings = standingsService.getDriverStandings(seasonId, "PRO");

        // Assert
        assertEquals(1, proStandings.size());
        assertEquals("Max Verstappen", proStandings.get(0).getDriverName());
        assertEquals("-", proStandings.get(0).getGap());
    }

    @Test
    void getDriverStandings_withMultipleEvents_computesGapAndPrevPosition() {
        // Evento 1: Checo (driver2) gana con 25 pts, Max (driver1) queda 2do con 18 pts
        RaceEvent event1 = new RaceEvent();
        event1.setId(UUID.randomUUID());
        event1.setSeason(season);
        event1.setDate(Instant.parse("2026-03-01T15:00:00Z"));
        event1.setRoundNumber("R01");

        RaceResult r1Event1 = RaceResult.builder()
                .id(UUID.randomUUID())
                .driver(driver2)
                .position(1)
                .points(25)
                .category("PRO")
                .raceEvent(event1)
                .build();
        RaceResult r2Event1 = RaceResult.builder()
                .id(UUID.randomUUID())
                .driver(driver1)
                .position(2)
                .points(18)
                .category("PRO")
                .raceEvent(event1)
                .build();
        event1.setRaceResults(List.of(r1Event1, r2Event1));

        // Evento 2: Max (driver1) gana con 25 pts, Checo (driver2) queda 3ro con 15 pts
        RaceEvent event2 = new RaceEvent();
        event2.setId(UUID.randomUUID());
        event2.setSeason(season);
        event2.setDate(Instant.parse("2026-03-15T15:00:00Z"));
        event2.setRoundNumber("R02");

        RaceResult r1Event2 = RaceResult.builder()
                .id(UUID.randomUUID())
                .driver(driver1)
                .position(1)
                .points(25)
                .category("PRO")
                .raceEvent(event2)
                .build();
        RaceResult r2Event2 = RaceResult.builder()
                .id(UUID.randomUUID())
                .driver(driver2)
                .position(3)
                .points(15)
                .category("PRO")
                .raceEvent(event2)
                .build();
        event2.setRaceResults(List.of(r1Event2, r2Event2));

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(raceEventRepository.findWithDetailsBySeasonId(seasonId)).thenReturn(List.of(event1, event2));

        // Act
        List<DriverStandingResponse> standings = standingsService.getDriverStandings(seasonId, null);

        // Assert:
        // Max total: 18 + 25 = 43 pts (P1 actual, P2 en R01) -> gap: "-", prevPosition: 2
        // Checo total: 25 + 15 = 40 pts (P2 actual, P1 en R01) -> gap: "-3", prevPosition: 1
        assertEquals(2, standings.size());

        DriverStandingResponse leader = standings.get(0);
        assertEquals(driver1Id, leader.getDriverId());
        assertEquals(43, leader.getPoints());
        assertEquals(1, leader.getPosition());
        assertEquals("-", leader.getGap());
        assertEquals(2, leader.getPrevPosition());

        DriverStandingResponse chaser = standings.get(1);
        assertEquals(driver2Id, chaser.getDriverId());
        assertEquals(40, chaser.getPoints());
        assertEquals(2, chaser.getPosition());
        assertEquals("-3", chaser.getGap());
        assertEquals(1, chaser.getPrevPosition());
    }

    @Test
    void getTeamStandings_success() {
        RaceResult result1 = new RaceResult();
        result1.setId(UUID.randomUUID());
        result1.setDriver(driver1);
        result1.setPosition(1);
        result1.setPoints(25);
        result1.setCategory("PRO");

        RaceResult result2 = new RaceResult();
        result2.setId(UUID.randomUUID());
        result2.setDriver(driver2);
        result2.setPosition(2);
        result2.setPoints(18);
        result2.setCategory("PRO");

        raceEvent.setRaceResults(List.of(result1, result2));

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(raceEventRepository.findWithDetailsBySeasonId(seasonId)).thenReturn(List.of(raceEvent));

        // Act
        List<TeamStandingResponse> teamStandings = standingsService.getTeamStandings(seasonId, null);

        // Assert: Ambos pilotos pertenecen a team1 (Red Bull Racing) -> 25 + 18 = 43 puntos
        assertEquals(1, teamStandings.size());
        TeamStandingResponse teamResponse = teamStandings.get(0);
        assertEquals(team1Id, teamResponse.getTeamId());
        assertEquals("Red Bull Racing", teamResponse.getTeamName());
        assertEquals(43, teamResponse.getPoints());
        assertEquals(1, teamResponse.getWins());
        assertEquals(2, teamResponse.getPodiums());
        assertEquals(1, teamResponse.getPosition());
        assertEquals("-", teamResponse.getGap());
        assertNull(teamResponse.getPrevPosition());
    }

    @Test
    void getTeamStandings_withMultipleEvents_computesGapAndPrevPosition() {
        Team team2 = new Team();
        team2.setId(UUID.randomUUID());
        team2.setName("Scuderia Ferrari");

        Driver driver3 = new Driver();
        driver3.setId(UUID.randomUUID());
        driver3.setName("Charles Leclerc");
        driver3.setTeam(team2);
        driver3.setStatus(DriverStatus.ACTIVE);

        // Evento 1: Ferrari gana con Leclerc (25 pts), Red Bull con Verstappen queda 2do (18 pts)
        RaceEvent event1 = new RaceEvent();
        event1.setId(UUID.randomUUID());
        event1.setSeason(season);
        event1.setDate(Instant.parse("2026-03-01T15:00:00Z"));
        event1.setRoundNumber("R01");

        RaceResult r1E1 = RaceResult.builder().id(UUID.randomUUID()).driver(driver3).position(1).points(25).raceEvent(event1).build();
        RaceResult r2E1 = RaceResult.builder().id(UUID.randomUUID()).driver(driver1).position(2).points(18).raceEvent(event1).build();
        event1.setRaceResults(List.of(r1E1, r2E1));

        // Evento 2: Red Bull gana con Verstappen (25 pts), Ferrari con Leclerc queda 3ro (15 pts)
        RaceEvent event2 = new RaceEvent();
        event2.setId(UUID.randomUUID());
        event2.setSeason(season);
        event2.setDate(Instant.parse("2026-03-15T15:00:00Z"));
        event2.setRoundNumber("R02");

        RaceResult r1E2 = RaceResult.builder().id(UUID.randomUUID()).driver(driver1).position(1).points(25).raceEvent(event2).build();
        RaceResult r2E2 = RaceResult.builder().id(UUID.randomUUID()).driver(driver3).position(3).points(15).raceEvent(event2).build();
        event2.setRaceResults(List.of(r1E2, r2E2));

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(raceEventRepository.findWithDetailsBySeasonId(seasonId)).thenReturn(List.of(event1, event2));

        // Act
        List<TeamStandingResponse> teamStandings = standingsService.getTeamStandings(seasonId, null);

        // Assert:
        // Red Bull: 18 + 25 = 43 pts (P1 actual, P2 en R01) -> gap: "-", prevPosition: 2
        // Ferrari: 25 + 15 = 40 pts (P2 actual, P1 en R01) -> gap: "-3", prevPosition: 1
        assertEquals(2, teamStandings.size());

        TeamStandingResponse redBull = teamStandings.get(0);
        assertEquals(team1Id, redBull.getTeamId());
        assertEquals(43, redBull.getPoints());
        assertEquals(1, redBull.getPosition());
        assertEquals("-", redBull.getGap());
        assertEquals(2, redBull.getPrevPosition());

        TeamStandingResponse ferrari = teamStandings.get(1);
        assertEquals(team2.getId(), ferrari.getTeamId());
        assertEquals(40, ferrari.getPoints());
        assertEquals(2, ferrari.getPosition());
        assertEquals("-3", ferrari.getGap());
        assertEquals(1, ferrari.getPrevPosition());
    }

    @Test
    void getDriverStandings_seasonNotFound_throwsException() {
        UUID unknownId = UUID.randomUUID();
        when(seasonRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> standingsService.getDriverStandings(unknownId, null));
    }
}
