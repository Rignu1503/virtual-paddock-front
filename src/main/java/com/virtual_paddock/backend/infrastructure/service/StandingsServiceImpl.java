package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.standings.DriverStandingResponse;
import com.virtual_paddock.backend.api.dtos.standings.TeamStandingResponse;
import com.virtual_paddock.backend.domain.entities.*;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SanctionRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IStandingsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StandingsServiceImpl implements IStandingsService {

    private final SeasonRepository seasonRepository;
    private final RaceEventRepository raceEventRepository;
    private final SanctionRepository sanctionRepository;

    @Override
    public List<DriverStandingResponse> getDriverStandings(Long seasonId, String category) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new EntityNotFoundException("Temporada no encontrada con ID: " + seasonId));

        List<RaceEvent> raceEvents = raceEventRepository.findBySeasonId(seasonId);
        if (raceEvents.isEmpty()) {
            return Collections.emptyList();
        }

        // Extraer todos los resultados de carrera de la temporada
        List<RaceResult> allResults = raceEvents.stream()
                .flatMap(re -> re.getRaceResults().stream())
                .filter(rr -> category == null || category.trim().isEmpty() || category.equalsIgnoreCase(rr.getCategory()))
                .toList();

        // Extraer sanciones con deducción de puntos
        List<Sanction> allSanctions = raceEvents.stream()
                .flatMap(re -> re.getSanctions().stream())
                .filter(s -> s.getPointsDeduction() != null && s.getPointsDeduction() > 0)
                .toList();

        // Mapear deducción de puntos por ID de piloto
        Map<Long, Integer> pointsDeductionsByDriver = allSanctions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDriver().getId(),
                        Collectors.summingInt(Sanction::getPointsDeduction)
                ));

        // Agrupar resultados por piloto
        Map<Driver, List<RaceResult>> resultsByDriver = allResults.stream()
                .collect(Collectors.groupingBy(RaceResult::getDriver));

        List<DriverStandingResponse> standings = new ArrayList<>();

        for (Map.Entry<Driver, List<RaceResult>> entry : resultsByDriver.entrySet()) {
            Driver driver = entry.getKey();
            List<RaceResult> driverResults = entry.getValue();

            int totalRacePoints = driverResults.stream()
                    .mapToInt(r -> r.getPoints() != null ? r.getPoints() : 0)
                    .sum();

            int deduction = pointsDeductionsByDriver.getOrDefault(driver.getId(), 0);
            int finalPoints = Math.max(0, totalRacePoints - deduction);

            int wins = (int) driverResults.stream()
                    .filter(r -> r.getPosition() != null && r.getPosition() == 1)
                    .count();

            int podiums = (int) driverResults.stream()
                    .filter(r -> r.getPosition() != null && r.getPosition() >= 1 && r.getPosition() <= 3)
                    .count();

            int fastestLaps = (int) driverResults.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getFastestLap()))
                    .count();

            int racesCount = driverResults.size();

            String driverCategory = driverResults.isEmpty() ? null : driverResults.get(0).getCategory();
            Team team = driver.getTeam();

            standings.add(DriverStandingResponse.builder()
                    .driverId(driver.getId())
                    .driverName(driver.getName())
                    .gamertag(driver.getGamertag())
                    .carNumber(driver.getCarNumber())
                    .carModel(driver.getCarModel())
                    .category(driverCategory)
                    .teamId(team != null ? team.getId() : null)
                    .teamName(team != null ? team.getName() : "Privado / Sin Equipo")
                    .points(finalPoints)
                    .wins(wins)
                    .podiums(podiums)
                    .fastestLaps(fastestLaps)
                    .polePositions(0)
                    .racesCount(racesCount)
                    .build());
        }

        // Ordenar con criterios de desempate oficiales:
        // 1º Puntos totales desc, 2º Victorias desc, 3º Podios desc, 4º Vueltas rápidas desc
        standings.sort(
                Comparator.comparing(DriverStandingResponse::getPoints).reversed()
                        .thenComparing(Comparator.comparing(DriverStandingResponse::getWins).reversed())
                        .thenComparing(Comparator.comparing(DriverStandingResponse::getPodiums).reversed())
                        .thenComparing(Comparator.comparing(DriverStandingResponse::getFastestLaps).reversed())
        );

        // Asignar posición en la tabla
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setPosition(i + 1);
        }

        return standings;
    }

    @Override
    public List<TeamStandingResponse> getTeamStandings(Long seasonId, String category) {
        List<DriverStandingResponse> driverStandings = getDriverStandings(seasonId, category);
        if (driverStandings.isEmpty()) {
            return Collections.emptyList();
        }

        // Agrupar los puntajes de los pilotos por Equipo
        Map<Long, List<DriverStandingResponse>> driversByTeam = driverStandings.stream()
                .filter(ds -> ds.getTeamId() != null)
                .collect(Collectors.groupingBy(DriverStandingResponse::getTeamId));

        List<TeamStandingResponse> teamStandings = new ArrayList<>();

        for (Map.Entry<Long, List<DriverStandingResponse>> entry : driversByTeam.entrySet()) {
            List<DriverStandingResponse> teamDrivers = entry.getValue();
            DriverStandingResponse first = teamDrivers.get(0);

            int combinedPoints = teamDrivers.stream().mapToInt(DriverStandingResponse::getPoints).sum();
            int combinedWins = teamDrivers.stream().mapToInt(DriverStandingResponse::getWins).sum();
            int combinedPodiums = teamDrivers.stream().mapToInt(DriverStandingResponse::getPodiums).sum();
            int maxRaces = teamDrivers.stream().mapToInt(DriverStandingResponse::getRacesCount).max().orElse(0);

            teamStandings.add(TeamStandingResponse.builder()
                    .teamId(first.getTeamId())
                    .teamName(first.getTeamName())
                    .carModel(first.getCarModel())
                    .category(first.getCategory())
                    .points(combinedPoints)
                    .wins(combinedWins)
                    .podiums(combinedPodiums)
                    .racesCount(maxRaces)
                    .build());
        }

        // Ordenar equipos por Puntos desc, Victorias desc, Podios desc
        teamStandings.sort(
                Comparator.comparing(TeamStandingResponse::getPoints).reversed()
                        .thenComparing(Comparator.comparing(TeamStandingResponse::getWins).reversed())
                        .thenComparing(Comparator.comparing(TeamStandingResponse::getPodiums).reversed())
        );

        for (int i = 0; i < teamStandings.size(); i++) {
            teamStandings.get(i).setPosition(i + 1);
        }

        return teamStandings;
    }
}
