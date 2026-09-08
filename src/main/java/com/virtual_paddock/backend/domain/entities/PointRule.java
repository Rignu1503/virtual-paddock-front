package com.virtual_paddock.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "point_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer position; // 1 para P1, 2 para P2, etc.

    @Column(nullable = false)
    private Integer points; // Cantidad de puntos otorgados

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_system_id", nullable = false)
    private PointsSystem pointsSystem;
}
