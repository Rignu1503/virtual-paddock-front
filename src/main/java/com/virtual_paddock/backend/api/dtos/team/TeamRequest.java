package com.virtual_paddock.backend.api.dtos.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamRequest {
    @NotBlank(message = "El nombre del equipo es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 100, message = "El modelo del auto no puede exceder 100 caracteres")
    private String carModel;

    @Size(max = 7, message = "El color debe ser un código hexadecimal válido")
    private String colorHex;

    @NotNull(message = "El ID de la liga es obligatorio")
    private UUID leagueId;
}
