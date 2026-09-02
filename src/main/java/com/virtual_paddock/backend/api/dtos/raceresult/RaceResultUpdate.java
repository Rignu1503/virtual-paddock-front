package com.virtual_paddock.backend.api.dtos.raceresult;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceResultUpdate {
    private Integer position;
    private Integer overallPosition;
    private String category;
    private String totalTime;
    private String finalTime;
    private String bestLapTime;
    private String gap;
    private Boolean fastestLap;
    private Integer points;
    private Integer penaltiesSeconds;
    private Integer lapsCompleted;
    private String status;
}
