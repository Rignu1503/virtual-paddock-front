package com.virtual_paddock.backend.api.dtos.pointssystem;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsSystemResponse {

    private Long id;
    private String name;
    private String description;
    private Integer fastestLapPoints;
    private Integer polePoints;
    private Long leagueId;
    private String leagueName;
    private List<PointRuleDto> rules;
}
