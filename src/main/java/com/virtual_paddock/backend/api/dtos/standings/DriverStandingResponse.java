package com.virtual_paddock.backend.api.dtos.standings;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverStandingResponse {
    private Integer position;          // Posición en la tabla (1, 2, 3...)
    private Integer prevPosition;      // Posición previa antes del último evento (null si <= 1 evento)
    private String gap;                // Diferencia con el líder ("-" para P1, "-N" para P2+)
    private UUID driverId;
    private String driverName;
    private String gamertag;
    private String nationality;
    private String carNumber;          // Dorsal (#)
    private String carModel;
    private String category;           // Categoría ("GT3", "LMP2", etc.)
    private UUID teamId;
    private String teamName;
    private Integer points;            // Puntos acumulados en la temporada
    private Integer wins;              // Victorias (P1)
    private Integer podiums;           // Podios (P1 a P3)
    private Integer fastestLaps;       // Vueltas rápidas marcadas
    private Integer polePositions;     // Poles
    private Integer racesCount;        // Carreras disputadas
}
