package com.virtual_paddock.backend.api.dtos.driver_registration;

import com.virtual_paddock.backend.utils.enums.DriverRegistrationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRegistrationResponse {

    private UUID id;
    private String name;
    private String gamertag;
    private String nationality;
    private String carNumber;
    private String carModel;
    private String email;
    private String discordTag;
    private String contactType;
    private String phoneWhatsapp;
    private String notes;
    private DriverRegistrationStatus status;

    private UUID leagueId;
    private String leagueName;

    private UUID championshipId;
    private String championshipName;

    private UUID preferredTeamId;
    private String preferredTeamName;

    private UUID assignedTeamId;
    private String assignedTeamName;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
}
