package com.virtual_paddock.backend.api.dtos.raceresult;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceResultBulkItemRequest {

    private UUID driverId;

    /**
     * Nombre del piloto (útil para previsualizaciones e importaciones).
     */
    private String driverName;

    /**
     * Dorsal / Número del auto.
     */
    private String carNumber;

    /**
     * Categoría en la carrera (ej: "GT3", "LMP2", "Hypercar").
     * Si es nulo, se puede tomar la del equipo o campeonato.
     */
    private String category;

    /**
     * Posición de llegada reportada en pista (opcional si se pasa totalTime).
     */
    private Integer finishOrder;

    /**
     * Tiempo en pista (ej: "45:10.500" o "1:23:45.123").
     */
    private String totalTime;

    /**
     * Tiempo de la mejor vuelta individual (ej: "1:42.345").
     */
    private String bestLapTime;

    /**
     * Segundos de penalización a sumar (ej: 5, 10).
     */
    @Builder.Default
    private Integer penaltiesSeconds = 0;

    /**
     * Indica si este piloto marcó la vuelta rápida de la carrera / categoría.
     */
    @Builder.Default
    private Boolean fastestLap = false;

    /**
     * Indica si este piloto obtuvo la pole position.
     */
    @Builder.Default
    private Boolean polePosition = false;

    /**
     * Vueltas completadas por el piloto en la carrera.
     */
    private Integer lapsCompleted;

    /**
     * Estado del piloto en la carrera ("FINISHED", "DNF", "DQ", "DNS").
     */
    @Builder.Default
    private String status = "FINISHED";
}
