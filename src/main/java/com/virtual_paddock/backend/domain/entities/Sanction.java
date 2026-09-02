package com.virtual_paddock.backend.domain.entities;

import com.virtual_paddock.backend.utils.enums.SanctionSeverity;
import com.virtual_paddock.backend.utils.enums.SanctionStatus;
import com.virtual_paddock.backend.utils.enums.SanctionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sanctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sanction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SanctionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SanctionSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SanctionStatus status;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "penalty_seconds")
    @Builder.Default
    private Integer penaltySeconds = 0; // Segundos de sanción aplicados a la carrera

    @Column(name = "points_deduction")
    @Builder.Default
    private Integer pointsDeduction = 0; // Puntos descontados de la tabla general

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_event_id", nullable = false)
    private RaceEvent raceEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
}
