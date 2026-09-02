package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkItemRequest;
import com.virtual_paddock.backend.domain.entities.Championship;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.RaceResult;
import com.virtual_paddock.backend.utils.enums.RaceType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RaceCalculationService {

    private static final List<Integer> DEFAULT_F1_POINTS = List.of(25, 18, 15, 12, 10, 8, 6, 4, 2, 1);
    private static final List<Integer> DEFAULT_SPRINT_POINTS = List.of(8, 7, 6, 5, 4, 3, 2, 1);

    /**
     * Procesa y calcula las posiciones (multiclase y general), aplica penalizaciones de tiempo y asigna puntos.
     */
    public List<RaceResult> calculateRaceResults(
            RaceEvent raceEvent,
            List<RaceResultBulkItemRequest> bulkItems,
            Map<Long, Driver> driversMap
    ) {
        Championship championship = raceEvent.getSeason().getChampionship();
        
        // Seleccionar escala de puntos según el tipo de carrera (NORMAL o SPRINT)
        boolean isSprint = raceEvent.getRaceType() == RaceType.SPRINT;
        List<Integer> pointsScale = resolvePointsScale(championship, isSprint);

        int fastestLapBonus = resolveFastestLapBonus(championship, isSprint);
        int poleBonus = resolvePoleBonus(championship, isSprint);

        // 1. Convertir cada item a una estructura intermedia con tiempo final calculado
        List<ProcessedItem> processedItems = new ArrayList<>();
        for (int i = 0; i < bulkItems.size(); i++) {
            RaceResultBulkItemRequest item = bulkItems.get(i);
            Driver driver = driversMap.get(item.getDriverId());
            if (driver == null) continue;

            // Determinar categoría (si no se especifica, toma la del equipo o campeonato)
            String category = item.getCategory();
            if (category == null || category.trim().isEmpty()) {
                category = (championship.getCategory() != null && !championship.getCategory().trim().isEmpty())
                        ? championship.getCategory()
                        : "GENERAL";
            }

            int penaltySec = item.getPenaltiesSeconds() != null ? item.getPenaltiesSeconds() : 0;
            Long rawTimeMs = parseTimeToMillis(item.getTotalTime());
            boolean simulatedTime = false;
            
            // Si no se proveyó tiempo (ej: DNF o entrada manual sin tiempos), simular tiempo por orden de llegada
            if (rawTimeMs == null) {
                simulatedTime = true;
                int order = item.getFinishOrder() != null ? item.getFinishOrder() : (i + 1);
                rawTimeMs = (long) order * 60000L; // 1 min por cada posición
            }

            Long finalTimeMs = rawTimeMs + (penaltySec * 1000L);
            String finalTimeFormatted = simulatedTime ? null : formatMillisToTime(finalTimeMs);

            processedItems.add(new ProcessedItem(
                    item,
                    driver,
                    category,
                    rawTimeMs,
                    penaltySec,
                    finalTimeMs,
                    finalTimeFormatted,
                    simulatedTime
            ));
        }

        // Comparador robusto para clasificaciones:
        // 1. Pilotos con tiempos reales (completaron carrera) van primero.
        // 2. Entre pilotos con tiempos reales, el que completó más vueltas va primero.
        // 3. Con igual cantidad de vueltas, se ordena por tiempo final corregido (menor a mayor).
        // 4. Pilotos con tiempos simulados (DNF/DQ/Manual) van después, ordenados por finishOrder.
        Comparator<ProcessedItem> itemComparator = (a, b) -> {
            if (a.simulatedTime() && !b.simulatedTime()) {
                return 1; // b va primero
            }
            if (!a.simulatedTime() && b.simulatedTime()) {
                return -1; // a va primero
            }
            if (a.simulatedTime() && b.simulatedTime()) {
                int orderA = a.request().getFinishOrder() != null ? a.request().getFinishOrder() : 999;
                int orderB = b.request().getFinishOrder() != null ? b.request().getFinishOrder() : 999;
                return Integer.compare(orderA, orderB);
            }
            // Si ambos tienen vueltas completadas, el que tenga más vueltas va primero
            Integer lapsA = a.request().getLapsCompleted();
            Integer lapsB = b.request().getLapsCompleted();
            if (lapsA != null && lapsB != null && !lapsA.equals(lapsB)) {
                return Integer.compare(lapsB, lapsA); // Mayor número de vueltas primero
            }
            return Long.compare(a.finalTimeMs(), b.finalTimeMs());
        };

        // 2. Ordenar general por la lógica del comparador
        processedItems.sort(itemComparator);
        for (int i = 0; i < processedItems.size(); i++) {
            processedItems.get(i).setOverallPosition(i + 1);
        }

        // 3. Agrupar y ordenar por categoría para calcular posición de clase y puntos
        Map<String, List<ProcessedItem>> byCategory = processedItems.stream()
                .collect(Collectors.groupingBy(ProcessedItem::category));

        List<RaceResult> results = new ArrayList<>();

        for (Map.Entry<String, List<ProcessedItem>> entry : byCategory.entrySet()) {
            String category = entry.getKey();
            List<ProcessedItem> categoryItems = entry.getValue();

            // Ordenar por categoría usando el mismo comparador robusto
            categoryItems.sort(itemComparator);

            ProcessedItem leader = categoryItems.isEmpty() ? null : categoryItems.get(0);
            boolean leaderSimulated = leader != null && leader.simulatedTime();

            for (int rank = 0; rank < categoryItems.size(); rank++) {
                ProcessedItem item = categoryItems.get(rank);
                int categoryPosition = rank + 1;

                String itemStatus = item.request().getStatus();
                if (itemStatus == null || itemStatus.trim().isEmpty()) {
                    itemStatus = item.simulatedTime() ? "DNF" : "FINISHED";
                }

                // Calcular puntos según la escala del campeonato
                int earnedPoints = calculateEarnedPoints(rank, pointsScale, fastestLapBonus, poleBonus, item, itemStatus);

                // Calcular Gap respecto al líder (tiempos, +1 LAP, +X LAPS, DNF, DQ)
                String gap = calculateGap(item, leader, rank, itemStatus, leaderSimulated);

                RaceResult result = buildRaceResult(raceEvent, item, category, categoryPosition, gap, earnedPoints, itemStatus);
                results.add(result);
            }
        }

        // Ordenar resultado final por posición general
        results.sort(Comparator.comparing(RaceResult::getOverallPosition));
        return results;
    }

    /**
     * Calcula la diferencia (GAP) con respecto al líder de la carrera o categoría.
     * Soporta diferencias exactas en milisegundos y vueltas perdidas (+1 LAP, +X LAPS).
     */
    public String calculateGap(
            ProcessedItem item,
            ProcessedItem leader,
            int rank,
            String itemStatus,
            boolean leaderSimulated
    ) {
        if ("DQ".equalsIgnoreCase(itemStatus)) {
            return "DQ";
        }
        if ("DNF".equalsIgnoreCase(itemStatus)) {
            return "DNF";
        }
        if ("DNS".equalsIgnoreCase(itemStatus)) {
            return "DNS";
        }
        if (rank == 0) {
            return item.simulatedTime() ? "" : "LEADER";
        }
        if (item.simulatedTime() || leaderSimulated || leader == null) {
            return "";
        }

        // Si hay conteo de vueltas y el piloto tiene menos vueltas que el líder
        Integer leaderLaps = leader.request().getLapsCompleted();
        Integer itemLaps = item.request().getLapsCompleted();
        if (leaderLaps != null && itemLaps != null && leaderLaps > itemLaps) {
            int lapDiff = leaderLaps - itemLaps;
            return lapDiff == 1 ? "+1 LAP" : "+" + lapDiff + " LAPS";
        }

        // Si están en la misma vuelta, calcular diferencia exacta en segundos (+X.XXX)
        long diffMs = item.finalTimeMs() - leader.finalTimeMs();
        return String.format("+%.3f", diffMs / 1000.0);
    }

    /**
     * Calcula los puntos obtenidos por el piloto según su puesto y bonificaciones.
     */
    public int calculateEarnedPoints(
            int rank,
            List<Integer> pointsScale,
            int fastestLapBonus,
            int poleBonus,
            ProcessedItem item,
            String itemStatus
    ) {
        if ("DQ".equalsIgnoreCase(itemStatus) || "DNS".equalsIgnoreCase(itemStatus)) {
            return 0; // Descalificado o No Largó -> 0 puntos
        }

        int earnedPoints = 0;
        if (rank < pointsScale.size()) {
            earnedPoints = pointsScale.get(rank);
        }

        // Sumar bonus de vuelta rápida
        if (Boolean.TRUE.equals(item.request().getFastestLap())) {
            earnedPoints += fastestLapBonus;
        }

        // Sumar bonus de pole
        if (Boolean.TRUE.equals(item.request().getPolePosition())) {
            earnedPoints += poleBonus;
        }

        return earnedPoints;
    }

    /**
     * Construye la entidad RaceResult con todos sus campos calculados.
     */
    private RaceResult buildRaceResult(
            RaceEvent raceEvent,
            ProcessedItem item,
            String category,
            int categoryPosition,
            String gap,
            int earnedPoints,
            String itemStatus
    ) {
        return RaceResult.builder()
                .raceEvent(raceEvent)
                .driver(item.driver())
                .category(category)
                .position(categoryPosition)
                .overallPosition(item.getOverallPosition())
                .totalTime(item.request().getTotalTime())
                .finalTime(item.finalTimeFormatted())
                .bestLapTime(item.request().getBestLapTime())
                .lapsCompleted(item.request().getLapsCompleted())
                .gap(gap)
                .fastestLap(Boolean.TRUE.equals(item.request().getFastestLap()))
                .penaltiesSeconds(item.penaltySeconds())
                .points(earnedPoints)
                .status(itemStatus)
                .build();
    }

    /**
     * Resuelve la escala de puntos desde el PointsSystem asignado o desde el string/defaults.
     */
    public List<Integer> resolvePointsScale(Championship championship, boolean isSprint) {
        if (isSprint) {
            if (championship.getSprintPointsSystemRef() != null && championship.getSprintPointsSystemRef().getRules() != null && !championship.getSprintPointsSystemRef().getRules().isEmpty()) {
                return championship.getSprintPointsSystemRef().getRules().stream()
                        .sorted(Comparator.comparing(com.virtual_paddock.backend.domain.entities.PointRule::getPosition))
                        .map(com.virtual_paddock.backend.domain.entities.PointRule::getPoints)
                        .toList();
            }
            return parsePointsSystem(championship.getSprintPointsSystem(), DEFAULT_SPRINT_POINTS);
        } else {
            if (championship.getPointsSystemRef() != null && championship.getPointsSystemRef().getRules() != null && !championship.getPointsSystemRef().getRules().isEmpty()) {
                return championship.getPointsSystemRef().getRules().stream()
                        .sorted(Comparator.comparing(com.virtual_paddock.backend.domain.entities.PointRule::getPosition))
                        .map(com.virtual_paddock.backend.domain.entities.PointRule::getPoints)
                        .toList();
            }
            return parsePointsSystem(championship.getPointsSystem(), DEFAULT_F1_POINTS);
        }
    }

    public int resolveFastestLapBonus(Championship championship, boolean isSprint) {
        if (isSprint) {
            if (championship.getSprintPointsSystemRef() != null && championship.getSprintPointsSystemRef().getFastestLapPoints() != null) {
                return championship.getSprintPointsSystemRef().getFastestLapPoints();
            }
        } else {
            if (championship.getPointsSystemRef() != null && championship.getPointsSystemRef().getFastestLapPoints() != null) {
                return championship.getPointsSystemRef().getFastestLapPoints();
            }
        }
        return championship.getFastestLapPoints() != null ? championship.getFastestLapPoints() : 0;
    }

    public int resolvePoleBonus(Championship championship, boolean isSprint) {
        if (isSprint) {
            if (championship.getSprintPointsSystemRef() != null && championship.getSprintPointsSystemRef().getPolePoints() != null) {
                return championship.getSprintPointsSystemRef().getPolePoints();
            }
        } else {
            if (championship.getPointsSystemRef() != null && championship.getPointsSystemRef().getPolePoints() != null) {
                return championship.getPointsSystemRef().getPolePoints();
            }
        }
        return championship.getPolePoints() != null ? championship.getPolePoints() : 0;
    }

    /**
     * Parsea la escala de puntos configurada por el administrador (ej: "25,18,15,12,10,8,6,4,2,1")
     */
    public List<Integer> parsePointsSystem(String pointsSystemStr, List<Integer> defaultPoints) {
        if (pointsSystemStr == null || pointsSystemStr.trim().isEmpty()) {
            return defaultPoints;
        }
        try {
            return Arrays.stream(pointsSystemStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
        } catch (Exception e) {
            return defaultPoints;
        }
    }

    public List<Integer> parsePointsSystem(String pointsSystemStr) {
        return parsePointsSystem(pointsSystemStr, DEFAULT_F1_POINTS);
    }

    /**
     * Parsea strings de tiempo como "45:10.500", "1:23:45.123" o "58.120" a milisegundos.
     */
    private Long parseTimeToMillis(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length == 3) {
                // hh:mm:ss.SSS
                long hours = Long.parseLong(parts[0]);
                long minutes = Long.parseLong(parts[1]);
                double seconds = Double.parseDouble(parts[2]);
                return (hours * 3600000L) + (minutes * 60000L) + (long)(seconds * 1000.0);
            } else if (parts.length == 2) {
                // mm:ss.SSS
                long minutes = Long.parseLong(parts[0]);
                double seconds = Double.parseDouble(parts[1]);
                return (minutes * 60000L) + (long)(seconds * 1000.0);
            } else if (parts.length == 1) {
                // ss.SSS
                double seconds = Double.parseDouble(parts[0]);
                return (long)(seconds * 1000.0);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Formatea milisegundos a string "mm:ss.SSS" o "hh:mm:ss.SSS".
     */
    private String formatMillisToTime(Long millis) {
        if (millis == null) return null;
        long totalSeconds = millis / 1000;
        long ms = millis % 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, ms);
        } else {
            return String.format("%02d:%02d.%03d", minutes, seconds, ms);
        }
    }

    private static class ProcessedItem {
        private final RaceResultBulkItemRequest request;
        private final Driver driver;
        private final String category;
        private final Long rawTimeMs;
        private final int penaltySeconds;
        private final Long finalTimeMs;
        private final String finalTimeFormatted;
        private final boolean simulatedTime;
        private Integer overallPosition;

        public ProcessedItem(RaceResultBulkItemRequest request, Driver driver, String category,
                             Long rawTimeMs, int penaltySeconds, Long finalTimeMs, String finalTimeFormatted,
                             boolean simulatedTime) {
            this.request = request;
            this.driver = driver;
            this.category = category;
            this.rawTimeMs = rawTimeMs;
            this.penaltySeconds = penaltySeconds;
            this.finalTimeMs = finalTimeMs;
            this.finalTimeFormatted = finalTimeFormatted;
            this.simulatedTime = simulatedTime;
        }

        public RaceResultBulkItemRequest request() { return request; }
        public Driver driver() { return driver; }
        public String category() { return category; }
        public int penaltySeconds() { return penaltySeconds; }
        public Long finalTimeMs() { return finalTimeMs; }
        public String finalTimeFormatted() { return finalTimeFormatted; }
        public boolean simulatedTime() { return simulatedTime; }
        public Integer getOverallPosition() { return overallPosition; }
        public void setOverallPosition(Integer overallPosition) { this.overallPosition = overallPosition; }
    }
}
