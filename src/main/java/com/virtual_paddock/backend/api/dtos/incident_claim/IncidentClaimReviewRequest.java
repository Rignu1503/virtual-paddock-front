package com.virtual_paddock.backend.api.dtos.incident_claim;

import com.virtual_paddock.backend.utils.enums.IncidentClaimStatus;
import com.virtual_paddock.backend.utils.enums.IncidentPenaltyType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentClaimReviewRequest {

    @NotNull(message = "El estado del veredicto es obligatorio")
    private IncidentClaimStatus status;

    private String stewardName;

    private String stewardNotes;

    private IncidentPenaltyType penaltyType;

    private Integer penaltySeconds;

    private Integer pointsDeduction;

    private String penaltyDetail;
}
