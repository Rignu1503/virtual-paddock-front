package com.virtual_paddock.backend.api.dtos.raceevent;

import com.virtual_paddock.backend.utils.enums.EventStatus;
import com.virtual_paddock.backend.utils.enums.RaceType;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceEventResponse {
    private Long id;
    private String roundNumber;
    private String circuitName;
    private LocalDate date;
    private EventStatus status;
    private RaceType raceType;
    private Long seasonId;
}
