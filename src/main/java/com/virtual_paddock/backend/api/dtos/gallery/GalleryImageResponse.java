package com.virtual_paddock.backend.api.dtos.gallery;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GalleryImageResponse {
    private UUID id;
    private String imageUrl;
    private String title;
    private String description;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private UUID leagueId;
}
