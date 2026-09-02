package com.virtual_paddock.backend.api.dtos.sanction;

import com.virtual_paddock.backend.utils.enums.SanctionSeverity;
import com.virtual_paddock.backend.utils.enums.SanctionStatus;
import com.virtual_paddock.backend.utils.enums.SanctionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanctionRequest {
    @NotNull(message = "El tipo de sanción es obligatorio")
    private SanctionType type;

    @NotNull(message = "La severidad es obligatoria")
    private SanctionSeverity severity;

    @NotNull(message = "El estado de la sanción es obligatorio")
    private SanctionStatus status;

    @Size(max = 500, message = "El detalle no puede exceder 500 caracteres")
    private String detail;

    private Integer penaltySeconds; // Segundos de penalización de tiempo (ej: 5, 10)

    private Integer pointsDeduction; // Puntos a descontar del campeonato

    @NotNull(message = "El ID del evento es obligatorio")
    private Long raceEventId;

    @NotNull(message = "El ID del piloto es obligatorio")
    private Long driverId;
}
