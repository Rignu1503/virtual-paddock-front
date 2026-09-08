package com.virtual_paddock.backend.api.dtos.raceresult;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceResultRequest {
    private Integer position;
    private Integer overallPosition;
    private String category;
    private String totalTime;
    private String finalTime;
    private String bestLapTime;
    private String gap;

    @NotNull(message = "Indicar vuelta rápida es obligatorio")
    private Boolean fastestLap;

    private Integer points;
    private Integer penaltiesSeconds;
    private Integer lapsCompleted;
    private String status;

    @NotNull(message = "El ID del evento es obligatorio")
    private UUID raceEventId;

    @NotNull(message = "El ID del piloto es obligatorio")
    private UUID driverId;
}
