package com.virtual_paddock.backend.api.dtos.team;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamUpdate {
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 100, message = "El modelo del auto no puede exceder 100 caracteres")
    private String carModel;

    @Size(max = 7, message = "El color debe ser un código hexadecimal válido")
    private String colorHex;
}
