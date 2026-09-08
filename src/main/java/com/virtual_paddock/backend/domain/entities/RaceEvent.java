package com.virtual_paddock.backend.domain.entities;

import com.virtual_paddock.backend.utils.enums.EventStatus;
import com.virtual_paddock.backend.utils.enums.RaceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "race_events", indexes = {
    @Index(name = "idx_race_events_season", columnList = "season_id"),
    @Index(name = "idx_race_events_season_status", columnList = "season_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String roundNumber;

    @Column(nullable = false)
    private String circuitName;

    private Instant date;

    @Column(name = "qualy_date")
    private Instant qualyDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "race_type", nullable = false)
    @Builder.Default
    private RaceType raceType = RaceType.NORMAL; // NORMAL o SPRINT

    @Column(name = "race_duration")
    @Builder.Default
    private String raceDuration = "45 Min";

    @Column(name = "qualy_duration")
    @Builder.Default
    private String qualyDuration = "15 Min";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @OneToMany(mappedBy = "raceEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RaceResult> raceResults = new ArrayList<>();

    @OneToMany(mappedBy = "raceEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Sanction> sanctions = new LinkedHashSet<>();
}
