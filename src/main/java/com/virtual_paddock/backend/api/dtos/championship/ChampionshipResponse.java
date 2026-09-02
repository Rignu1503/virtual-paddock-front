package com.virtual_paddock.backend.api.dtos.championship;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionshipResponse {

    private Long id;
    private String gameName;
    private String category;
    private String pointsSystem;
    private String sprintPointsSystem;
    private Integer fastestLapPoints;
    private Integer polePoints;
    private Long pointsSystemId;
    private String pointsSystemName;
    private Long sprintPointsSystemId;
    private String sprintPointsSystemName;
    private Long leagueId;
    private String leagueName;
}
