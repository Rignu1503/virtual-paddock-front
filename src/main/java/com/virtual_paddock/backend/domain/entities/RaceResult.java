package com.virtual_paddock.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "race_results", indexes = {
    @Index(name = "idx_race_results_event", columnList = "race_event_id"),
    @Index(name = "idx_race_results_driver", columnList = "driver_id"),
    @Index(name = "idx_race_results_event_pos", columnList = "race_event_id, position")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer position; // Posición en su categoría
    
    private Integer overallPosition; // Posición general en pista

    private String category; // Categoría en la carrera (ej: "GT3", "LMP2")

    private String totalTime; // Tiempo en pista (ej: "45:10.500")

    private String finalTime; // Tiempo tras sumar penalizaciones (ej: "45:15.500")

    private String gap;

    private String bestLapTime; // Tiempo de la mejor vuelta del piloto (ej: "1:42.345")

    @Column(nullable = false)
    private Boolean fastestLap;

    private Integer points;

    private Integer penaltiesSeconds;

    private Integer lapsCompleted;

    private String status; // FINISHED, DNF, DQ, DNS

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_event_id", nullable = false)
    private RaceEvent raceEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
}
