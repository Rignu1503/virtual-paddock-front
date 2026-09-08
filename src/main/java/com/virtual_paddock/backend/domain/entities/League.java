package com.virtual_paddock.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leagues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slugUrl;

    private String tagline;

    private String accentColor;

    private String surfaceTheme;

    private String discordUrl;

    private String twitchUrl;

    private String kickUrl;

    private String tiktokUrl;

    private String youtubeUrl;

    private String registrationUrl;

    private String rulesUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Championship> championships = new ArrayList<>();

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Team> teams = new ArrayList<>();
}
