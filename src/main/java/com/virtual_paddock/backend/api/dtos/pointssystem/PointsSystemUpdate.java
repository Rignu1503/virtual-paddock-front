package com.virtual_paddock.backend.api.dtos.pointssystem;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsSystemUpdate {

    private String name;
    private String description;

    @Min(value = 0, message = "Los puntos de vuelta rápida no pueden ser negativos")
    private Integer fastestLapPoints;

    @Min(value = 0, message = "Los puntos de pole position no pueden ser negativos")
    private Integer polePoints;

    @Valid
    private List<PointRuleDto> rules;
}
