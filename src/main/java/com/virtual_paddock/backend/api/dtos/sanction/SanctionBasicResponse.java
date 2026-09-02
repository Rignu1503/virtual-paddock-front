package com.virtual_paddock.backend.api.dtos.sanction;

import com.virtual_paddock.backend.utils.enums.SanctionSeverity;
import com.virtual_paddock.backend.utils.enums.SanctionType;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanctionBasicResponse {
    private Long id;
    private SanctionType type;
    private SanctionSeverity severity;
    private com.virtual_paddock.backend.utils.enums.SanctionStatus status;
    private String detail;
    private String driverName;
}
