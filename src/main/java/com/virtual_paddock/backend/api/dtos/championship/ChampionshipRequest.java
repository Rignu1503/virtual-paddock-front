package com.virtual_paddock.backend.api.dtos.championship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionshipRequest {

    @NotBlank(message = "El nombre del juego es obligatorio")
    @Size(max = 100, message = "El nombre del juego no puede exceder 100 caracteres")
    private String gameName;

    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String category;

    private String pointsSystem; // Escala normal ej: "25,18,15,12,10,8,6,4,2,1"

    private String sprintPointsSystem; // Escala sprint ej: "8,7,6,5,4,3,2,1"

    private Integer fastestLapPoints; // Puntos por vuelta rápida

    private Integer polePoints; // Puntos por pole position

    private Long pointsSystemId;
    private Long sprintPointsSystemId;

    @NotNull(message = "El ID de la liga es obligatorio")
    private Long leagueId;
}
