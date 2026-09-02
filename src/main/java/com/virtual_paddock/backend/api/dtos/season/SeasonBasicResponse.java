package com.virtual_paddock.backend.api.dtos.season;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonBasicResponse {
    private Long id;
    private String seasonName;
}
