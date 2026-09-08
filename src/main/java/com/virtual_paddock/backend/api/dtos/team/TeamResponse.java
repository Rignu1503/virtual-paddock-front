package com.virtual_paddock.backend.api.dtos.team;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamResponse {
    private UUID id;
    private String name;
    private String carModel;
    private String colorHex;
    private UUID leagueId;
}
