package com.virtual_paddock.backend.api.dtos.pointssystem;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsSystemResponse {

    private UUID id;
    private String name;
    private String description;
    private Integer fastestLapPoints;
    private Integer polePoints;
    private UUID leagueId;
    private String leagueName;
    private List<PointRuleDto> rules;
}
