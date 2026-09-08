package com.virtual_paddock.backend.api.dtos.raceevent;

import com.virtual_paddock.backend.utils.enums.RaceType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceEventBasicResponse {
    private UUID id;
    private String roundNumber;
    private String circuitName;
    private RaceType raceType;
    private Instant date;
    private Instant qualyDate;
    private com.virtual_paddock.backend.utils.enums.EventStatus status;
    private String raceDuration;
    private String qualyDuration;
}
