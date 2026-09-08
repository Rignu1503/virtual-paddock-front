package com.virtual_paddock.backend.api.dtos.sanction;

import com.virtual_paddock.backend.utils.enums.SanctionSeverity;
import com.virtual_paddock.backend.utils.enums.SanctionStatus;
import com.virtual_paddock.backend.utils.enums.SanctionType;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanctionResponse {
    private UUID id;
    private SanctionType type;
    private SanctionSeverity severity;
    private SanctionStatus status;
    private String detail;
    private Integer penaltySeconds;
    private Integer pointsDeduction;
    private UUID raceEventId;
    private UUID driverId;
    private String driverName;
}
