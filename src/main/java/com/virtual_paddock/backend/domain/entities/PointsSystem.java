package com.virtual_paddock.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "points_systems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "fastest_lap_points")
    @Builder.Default
    private Integer fastestLapPoints = 1;

    @Column(name = "pole_points")
    @Builder.Default
    private Integer polePoints = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @OneToMany(mappedBy = "pointsSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<PointRule> rules = new ArrayList<>();
}
