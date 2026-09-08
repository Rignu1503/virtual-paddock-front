package com.virtual_paddock.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "championships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Championship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String gameName;

    private String category;

    @Column(name = "points_system")
    private String pointsSystem; // Escala carrera normal ej: "25,18,15,12,10,8,6,4,2,1"

    @Column(name = "sprint_points_system")
    private String sprintPointsSystem; // Escala carrera sprint ej: "8,7,6,5,4,3,2,1"

    @Column(name = "fastest_lap_points")
    @Builder.Default
    private Integer fastestLapPoints = 1; // Puntos adicionales por vuelta rápida

    @Column(name = "pole_points")
    @Builder.Default
    private Integer polePoints = 0; // Puntos adicionales por pole

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_system_id")
    private PointsSystem pointsSystemRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_points_system_id")
    private PointsSystem sprintPointsSystemRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @OneToMany(mappedBy = "championship", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Season> seasons = new ArrayList<>();
}
