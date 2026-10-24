package com.virtual_paddock.backend.api.dtos.incident_claim;

import com.virtual_paddock.backend.utils.enums.IncidentClaimStatus;
import com.virtual_paddock.backend.utils.enums.IncidentClaimType;
import com.virtual_paddock.backend.utils.enums.IncidentPenaltyType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentClaimResponse {
    private UUID id;
    private String ticketNumber;
    private UUID raceEventId;
    private String raceName;
    private String roundNumber;
    private UUID championshipId;
    private String championshipName;
    private UUID leagueId;
    private String reportingDriverName;
    private String reportingDriverSteamId;
    private UUID reportedDriverId;
    private String reportedDriverName;
    private String lapOrMinute;
    private String videoUrl;
    private IncidentClaimType incidentType;
    private String description;
    private IncidentClaimStatus status;
    private IncidentPenaltyType penaltyType;
    private Integer penaltySeconds;
    private Integer pointsDeduction;
    private String penaltyDetail;
    private String stewardName;
    private String stewardNotes;
    private UUID sanctionId;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
