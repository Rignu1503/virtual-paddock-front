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
    private Integer prevPosition;      // Posición previa antes del último evento (null si <= 1 evento)
    private String gap;                // Diferencia con el líder ("-" para P1, "-N" para P2+)
    private UUID teamId;
    private String teamName;
    private String carModel;
    private String category;
    private Integer points;            // Puntos combinados de todos los pilotos del equipo
    private Integer wins;              // Victorias combinadas
    private Integer podiums;           // Podios combinados
    private Integer racesCount;        // Carreras disputadas
}
