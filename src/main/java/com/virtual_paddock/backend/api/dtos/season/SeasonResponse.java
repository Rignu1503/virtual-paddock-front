package com.virtual_paddock.backend.api.dtos.season;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonResponse {
    private UUID id;
    private String seasonName;
    private UUID championshipId;
}
