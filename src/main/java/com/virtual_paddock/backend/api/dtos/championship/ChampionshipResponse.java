package com.virtual_paddock.backend.api.dtos.championship;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionshipResponse {

    private UUID id;
    private String gameName;
    private String category;
    private String pointsSystem;
    private String sprintPointsSystem;
    private Integer fastestLapPoints;
    private Integer polePoints;
    private UUID pointsSystemId;
    private String pointsSystemName;
    private UUID sprintPointsSystemId;
    private String sprintPointsSystemName;
    private UUID leagueId;
    private String leagueName;
}
