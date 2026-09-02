package com.virtual_paddock.backend.api.dtos.season;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonResponse {
    private Long id;
    private String seasonName;
    private Long championshipId;
}
