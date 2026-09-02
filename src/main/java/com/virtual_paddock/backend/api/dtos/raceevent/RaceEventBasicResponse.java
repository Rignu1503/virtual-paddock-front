package com.virtual_paddock.backend.api.dtos.raceevent;

import com.virtual_paddock.backend.utils.enums.RaceType;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceEventBasicResponse {
    private Long id;
    private String roundNumber;
    private String circuitName;
    private RaceType raceType;
    private String date;
    private com.virtual_paddock.backend.utils.enums.EventStatus status;
}
