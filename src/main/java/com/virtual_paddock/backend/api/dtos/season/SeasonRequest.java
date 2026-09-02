package com.virtual_paddock.backend.api.dtos.season;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonRequest {
    @NotBlank(message = "El nombre de la temporada es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String seasonName;

    @NotNull(message = "El ID del campeonato es obligatorio")
    private Long championshipId;
}
