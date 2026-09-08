package com.virtual_paddock.backend.api.dtos.raceevent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.virtual_paddock.backend.infrastructure.config.FlexibleInstantDeserializer;
import com.virtual_paddock.backend.utils.enums.EventStatus;
import com.virtual_paddock.backend.utils.enums.RaceType;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceEventUpdate {
    @Size(max = 10, message = "El número de ronda no puede exceder 10 caracteres")
    private String roundNumber;

    @Size(max = 150, message = "El nombre del circuito no puede exceder 150 caracteres")
    private String circuitName;

    @JsonDeserialize(using = FlexibleInstantDeserializer.class)
    private Instant date;

    @JsonDeserialize(using = FlexibleInstantDeserializer.class)
    private Instant qualyDate;
    private EventStatus status;
    private RaceType raceType;
    private String raceDuration;
    private String qualyDuration;
}
