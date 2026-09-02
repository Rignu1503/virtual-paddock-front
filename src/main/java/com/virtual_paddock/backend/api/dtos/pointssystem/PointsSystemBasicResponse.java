package com.virtual_paddock.backend.api.dtos.pointssystem;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsSystemBasicResponse {

    private Long id;
    private String name;
    private String description;
    private Integer fastestLapPoints;
    private Integer polePoints;
    private Integer rulesCount;
}
