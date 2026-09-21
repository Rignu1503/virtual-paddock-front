package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.gallery.*;
import com.virtual_paddock.backend.domain.entities.GalleryImage;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface GalleryImageMapper {

    @Mapping(target = "leagueId", source = "league.id")
    GalleryImageResponse toResponse(GalleryImage galleryImage);

    GalleryImageBasicResponse toBasicResponse(GalleryImage galleryImage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    GalleryImage toEntity(GalleryImageRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(GalleryImageUpdate update, @MappingTarget GalleryImage galleryImage);
}
