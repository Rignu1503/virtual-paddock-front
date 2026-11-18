package com.virtual_paddock.backend.api.dtos.season;

import com.virtual_paddock.backend.utils.enums.SeasonStatus;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonBasicResponse {
    private UUID id;
    private String seasonName;
    private Boolean isDefault;
    private String categories;
    private SeasonStatus status;
}
