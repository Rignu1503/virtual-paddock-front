package com.virtual_paddock.backend.api.dtos.season;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonUpdate {
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String seasonName;
}
