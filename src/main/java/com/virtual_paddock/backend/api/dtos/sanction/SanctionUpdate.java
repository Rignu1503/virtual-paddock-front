package com.virtual_paddock.backend.api.dtos.sanction;

import com.virtual_paddock.backend.utils.enums.SanctionSeverity;
import com.virtual_paddock.backend.utils.enums.SanctionStatus;
import com.virtual_paddock.backend.utils.enums.SanctionType;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanctionUpdate {
    private SanctionType type;
    private SanctionSeverity severity;
    private SanctionStatus status;

    @Size(max = 500, message = "El detalle no puede exceder 500 caracteres")
    private String detail;

    private Integer penaltySeconds;
    private Integer pointsDeduction;
}
