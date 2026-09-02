package com.virtual_paddock.backend.api.dtos.raceresult;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceResultResponse {
    private Long id;
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
    private Long raceEventId;
    private Long driverId;
    private String driverName;
}
