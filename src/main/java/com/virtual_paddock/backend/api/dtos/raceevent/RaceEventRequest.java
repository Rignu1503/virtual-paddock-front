package com.virtual_paddock.backend.api.dtos.raceevent;

import com.virtual_paddock.backend.utils.enums.EventStatus;
import com.virtual_paddock.backend.utils.enums.RaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RaceEventRequest {
    @NotBlank(message = "El número de ronda es obligatorio")
    @Size(max = 10, message = "El número de ronda no puede exceder 10 caracteres")
    private String roundNumber;

    @NotBlank(message = "El nombre del circuito es obligatorio")
    @Size(max = 150, message = "El nombre del circuito no puede exceder 150 caracteres")
    private String circuitName;

    private LocalDate date;

    @NotNull(message = "El estado del evento es obligatorio")
    private EventStatus status;

    @Builder.Default
    private RaceType raceType = RaceType.NORMAL;

    @NotNull(message = "El ID de la temporada es obligatorio")
    private Long seasonId;
}
