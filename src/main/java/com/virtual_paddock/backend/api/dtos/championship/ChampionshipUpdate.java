package com.virtual_paddock.backend.api.dtos.championship;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionshipUpdate {

    @Size(max = 100, message = "El nombre del juego no puede exceder 100 caracteres")
    private String gameName;

    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    private String category;

    private String pointsSystem;

    private String sprintPointsSystem;

    private Integer fastestLapPoints;

    private Integer polePoints;

    private Long pointsSystemId;

    private Long sprintPointsSystemId;
}
