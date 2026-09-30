package com.virtual_paddock.backend.api.dtos.championship;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionshipBasicResponse {

    private UUID id;
    private String gameName;
    private String category;
    private Integer maxDriversPerTeam;
    private Boolean requireSteamId;
}
