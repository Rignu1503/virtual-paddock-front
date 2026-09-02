package com.virtual_paddock.backend.api.dtos.pointssystem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointRuleDto {

    @NotNull(message = "La posición es obligatoria")
    @Min(value = 1, message = "La posición debe ser al menos 1")
    private Integer position; // 1 para P1, 2 para P2, etc.

    @NotNull(message = "La cantidad de puntos es obligatoria")
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    private Integer points;
}
