package com.virtual_paddock.backend.api.dtos.gallery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GalleryImageRequest {
    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Size(max = 2048, message = "La URL no puede exceder 2048 caracteres")
    private String imageUrl;

    @Size(max = 150, message = "El título no puede exceder 150 caracteres")
    private String title;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    private Integer displayOrder;

    @NotNull(message = "El ID de la liga es obligatorio")
    private UUID leagueId;
}
