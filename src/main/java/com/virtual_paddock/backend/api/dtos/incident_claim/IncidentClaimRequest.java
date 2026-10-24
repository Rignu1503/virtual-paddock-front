package com.virtual_paddock.backend.api.dtos.incident_claim;

import com.virtual_paddock.backend.utils.enums.IncidentClaimType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentClaimRequest {

    @NotNull(message = "El ID del evento de carrera es obligatorio")
    private UUID raceEventId;

    @NotBlank(message = "El nombre del piloto denunciante es obligatorio")
    private String reportingDriverName;

    private String reportingDriverSteamId;

    private UUID reportedDriverId;

    @NotBlank(message = "El nombre del piloto denunciado es obligatorio")
    private String reportedDriverName;

    private String lapOrMinute;

    private String videoUrl;

    @NotNull(message = "El tipo de incidente es obligatorio")
    private IncidentClaimType incidentType;

    @NotBlank(message = "La descripción del incidente es obligatoria")
    private String description;
}
