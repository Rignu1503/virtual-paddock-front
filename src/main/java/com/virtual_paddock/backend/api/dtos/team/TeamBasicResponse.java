package com.virtual_paddock.backend.api.dtos.team;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamBasicResponse {
    private Long id;
    private String name;
}
