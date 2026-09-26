package com.virtual_paddock.backend.domain.entities;

import com.virtual_paddock.backend.utils.enums.DriverRegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_registrations", indexes = {
    @Index(name = "idx_reg_league", columnList = "league_id"),
    @Index(name = "idx_reg_championship", columnList = "championship_id"),
    @Index(name = "idx_reg_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String gamertag;

    @Column(length = 50)
    private String nationality;

    @Column(name = "car_number", length = 10)
    private String carNumber;

    @Column(name = "car_model", length = 100)
    private String carModel;

    @Column(length = 150)
    private String email;

    @Column(name = "discord_tag", length = 100)
    private String discordTag;

    @Column(name = "contact_type", length = 20)
    private String contactType; // "DISCORD" | "WHATSAPP"

    @Column(name = "phone_whatsapp", length = 50)
    private String phoneWhatsapp;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DriverRegistrationStatus status = DriverRegistrationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "championship_id")
    private Championship championship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_team_id")
    private Team preferredTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}
