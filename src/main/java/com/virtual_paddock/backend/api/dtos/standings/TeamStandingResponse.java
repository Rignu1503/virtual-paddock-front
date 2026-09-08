package com.virtual_paddock.backend.api.dtos.standings;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamStandingResponse {
    private Integer position;          // Posición en la tabla de constructores
    private UUID teamId;
    private String teamName;
    private String carModel;
    private String category;
    private Integer points;            // Puntos combinados de todos los pilotos del equipo
    private Integer wins;              // Victorias combinadas
    private Integer podiums;           // Podios combinados
    private Integer racesCount;        // Carreras disputadas
}
