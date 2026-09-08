package com.virtual_paddock.backend.api.dtos.season;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonBasicResponse {
    private UUID id;
    private String seasonName;
}
