package com.virtual_paddock.backend.api.dtos.gallery;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GalleryImageUpdate {
    @Size(max = 2048, message = "La URL no puede exceder 2048 caracteres")
    private String imageUrl;

    @Size(max = 150, message = "El título no puede exceder 150 caracteres")
    private String title;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    private Integer displayOrder;
}
