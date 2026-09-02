package com.virtual_paddock.backend.api.dtos.pointssystem;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsSystemRequest {

    @NotBlank(message = "El nombre del sistema de puntos es obligatorio")
    private String name;

    private String description;

    @Min(value = 0, message = "Los puntos de vuelta rápida no pueden ser negativos")
    @Builder.Default
    private Integer fastestLapPoints = 1;

    @Min(value = 0, message = "Los puntos de pole position no pueden ser negativos")
    @Builder.Default
    private Integer polePoints = 0;

    @NotNull(message = "El ID de la liga es obligatorio")
    private Long leagueId;

    @NotEmpty(message = "Debe definir al menos una regla de puntaje")
    @Valid
    private List<PointRuleDto> rules;
}
