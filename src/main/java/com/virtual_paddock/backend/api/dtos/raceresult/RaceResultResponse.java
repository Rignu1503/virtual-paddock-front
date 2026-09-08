package com.virtual_paddock.backend.api.dtos.raceresult;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceResultResponse {
    private UUID id;
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
    private UUID raceEventId;
    private UUID driverId;
    private String driverName;
}
