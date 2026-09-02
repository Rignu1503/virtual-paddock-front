package com.virtual_paddock.backend.domain.entities;

import com.virtual_paddock.backend.utils.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String gamertag;

    private String nationality;

    @Column(name = "car_number")
    private String carNumber; // Dorsal oficial ej: "44", "1", "99"

    @Column(name = "car_model")
    private String carModel; // Modelo de auto ej: "Porsche 992 GT3 R"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RaceResult> raceResults = new ArrayList<>();

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sanction> sanctions = new ArrayList<>();
}
