package com.virtual_paddock.backend.api.dtos.team;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamBasicResponse {
    private UUID id;
    private String name;
    private String carModel;
    private String colorHex;
}
