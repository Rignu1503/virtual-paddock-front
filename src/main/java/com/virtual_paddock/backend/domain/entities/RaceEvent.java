package com.virtual_paddock.backend.domain.entities;

import com.virtual_paddock.backend.utils.enums.EventStatus;
import com.virtual_paddock.backend.utils.enums.RaceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "race_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roundNumber;

    @Column(nullable = false)
    private String circuitName;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "race_type", nullable = false)
    @Builder.Default
    private RaceType raceType = RaceType.NORMAL; // NORMAL o SPRINT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @OneToMany(mappedBy = "raceEvent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RaceResult> raceResults = new ArrayList<>();

    @OneToMany(mappedBy = "raceEvent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sanction> sanctions = new ArrayList<>();
}
