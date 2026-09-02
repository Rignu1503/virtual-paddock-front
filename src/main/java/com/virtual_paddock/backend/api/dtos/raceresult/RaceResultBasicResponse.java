package com.virtual_paddock.backend.api.dtos.raceresult;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceResultBasicResponse {
    private Long id;
    private Integer position;
    private Integer overallPosition;
    private String category;
    private Long driverId;
    private String driverName;
    private String totalTime;
    private String gap;
    private String bestLapTime;
    private Integer lapsCompleted;
    private Integer points;
    private Integer penaltiesSeconds;
    private String status;
}
