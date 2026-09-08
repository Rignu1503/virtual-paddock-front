package com.virtual_paddock.backend.infrastructure.helper;

import com.virtual_paddock.backend.api.dtos.standings.DriverStandingResponse;
import com.virtual_paddock.backend.api.dtos.standings.TeamStandingResponse;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.RaceResult;
import com.virtual_paddock.backend.domain.entities.Sanction;
import com.virtual_paddock.backend.domain.entities.Team;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class StandingsCalculatorHelper {

    /**
     * Calcula la clasificación de pilotos a partir de los eventos de carrera y sanciones.
     */
    public List<DriverStandingResponse> calculateDriverStandings(List<RaceEvent> raceEvents, String category) {
        if (raceEvents == null || raceEvents.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Extraer resultados de carrera filtrados por categoría opcional
        List<RaceResult> allResults = raceEvents.stream()
                .filter(re -> re.getRaceResults() != null)
                .flatMap(re -> re.getRaceResults().stream())
                .filter(rr -> category == null || category.trim().isEmpty() || category.equalsIgnoreCase(rr.getCategory()))
                .toList();

        // 2. Extraer sanciones con deducción de puntos
        List<Sanction> allSanctions = raceEvents.stream()
                .filter(re -> re.getSanctions() != null)
                .flatMap(re -> re.getSanctions().stream())
                .filter(s -> s.getPointsDeduction() != null && s.getPointsDeduction() > 0 && s.getDriver() != null)
                .toList();

        // 3. Mapear deducción de puntos por ID de piloto
        Map<UUID, Integer> pointsDeductionsByDriver = allSanctions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDriver().getId(),
                        Collectors.summingInt(Sanction::getPointsDeduction)
                ));

        // 4. Agrupar resultados por piloto
        Map<Driver, List<RaceResult>> resultsByDriver = allResults.stream()
                .filter(rr -> rr.getDriver() != null)
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
                    .nationality(driver.getNationality())
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

        // 5. Criterios oficiales de desempate: Puntos desc, Victorias desc, Podios desc, Vueltas rápidas desc
        standings.sort(
                Comparator.comparing(DriverStandingResponse::getPoints).reversed()
                        .thenComparing(Comparator.comparing(DriverStandingResponse::getWins).reversed())
                        .thenComparing(Comparator.comparing(DriverStandingResponse::getPodiums).reversed())
                        .thenComparing(Comparator.comparing(DriverStandingResponse::getFastestLaps).reversed())
        );

        // 6. Asignar posiciones ordinales
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setPosition(i + 1);
        }

        return standings;
    }

    /**
     * Calcula la clasificación de equipos/escuderías a partir de la tabla de pilotos.
     */
    public List<TeamStandingResponse> calculateTeamStandings(List<DriverStandingResponse> driverStandings) {
        if (driverStandings == null || driverStandings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, List<DriverStandingResponse>> driversByTeam = driverStandings.stream()
                .filter(ds -> ds.getTeamId() != null)
                .collect(Collectors.groupingBy(DriverStandingResponse::getTeamId));

        List<TeamStandingResponse> teamStandings = new ArrayList<>();

        for (Map.Entry<UUID, List<DriverStandingResponse>> entry : driversByTeam.entrySet()) {
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
