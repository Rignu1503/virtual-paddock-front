package com.virtual_paddock.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seasonName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "championship_id", nullable = false)
    private Championship championship;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RaceEvent> raceEvents = new ArrayList<>();
}
