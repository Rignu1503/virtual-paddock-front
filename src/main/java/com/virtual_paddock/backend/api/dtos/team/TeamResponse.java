package com.virtual_paddock.backend.api.dtos.team;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamResponse {
    private Long id;
    private String name;
    private String carModel;
    private String colorHex;
    private Long leagueId;
}
