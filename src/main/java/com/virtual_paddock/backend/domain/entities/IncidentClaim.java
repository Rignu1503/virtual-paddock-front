package com.virtual_paddock.backend.domain.entities;

import com.virtual_paddock.backend.utils.enums.IncidentClaimStatus;
import com.virtual_paddock.backend.utils.enums.IncidentClaimType;
import com.virtual_paddock.backend.utils.enums.IncidentPenaltyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "incident_claims", indexes = {
    @Index(name = "idx_claims_event", columnList = "race_event_id"),
    @Index(name = "idx_claims_status", columnList = "status"),
    @Index(name = "idx_claims_ticket", columnList = "ticket_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 50)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_event_id", nullable = false)
    private RaceEvent raceEvent;

    @Column(name = "reporting_driver_name", nullable = false, length = 100)
    private String reportingDriverName;

    @Column(name = "reporting_driver_steam_id", length = 100)
    private String reportingDriverSteamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_driver_id")
    private Driver reportedDriver;

    @Column(name = "reported_driver_name", nullable = false, length = 100)
    private String reportedDriverName;

    @Column(name = "lap_or_minute", length = 50)
    private String lapOrMinute;

    @Column(name = "video_url", length = 2048)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private IncidentClaimType incidentType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IncidentClaimStatus status = IncidentClaimStatus.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_type")
    @Builder.Default
    private IncidentPenaltyType penaltyType = IncidentPenaltyType.NINGUNA;

    @Column(name = "penalty_seconds")
    @Builder.Default
    private Integer penaltySeconds = 0;

    @Column(name = "points_deduction")
    @Builder.Default
    private Integer pointsDeduction = 0;

    @Column(name = "penalty_detail", length = 500)
    private String penaltyDetail;

    @Column(name = "steward_name", length = 100)
    private String stewardName;

    @Column(name = "steward_notes", columnDefinition = "TEXT")
    private String stewardNotes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sanction_id")
    private Sanction sanction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = IncidentClaimStatus.PENDIENTE;
        }
        if (this.penaltyType == null) {
            this.penaltyType = IncidentPenaltyType.NINGUNA;
        }
        if (this.penaltySeconds == null) {
            this.penaltySeconds = 0;
        }
        if (this.pointsDeduction == null) {
            this.pointsDeduction = 0;
        }
    }
}
