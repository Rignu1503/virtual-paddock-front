package com.virtual_paddock.backend.api.dtos.raceevent;

import com.virtual_paddock.backend.utils.enums.EventStatus;
import com.virtual_paddock.backend.utils.enums.RaceType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceEventResponse {
    private UUID id;
    private String roundNumber;
    private String circuitName;
    private Instant date;
    private Instant qualyDate;
    private EventStatus status;
    private RaceType raceType;
    private String raceDuration;
    private String qualyDuration;
    private UUID seasonId;
}
