package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.gallery.*;
import com.virtual_paddock.backend.api.dtos.PageResponse;

import java.util.UUID;

public interface IGalleryImageService {
    GalleryImageResponse create(GalleryImageRequest request);
    GalleryImageResponse getById(UUID id);
    PageResponse<GalleryImageBasicResponse> getByLeagueId(UUID leagueId, int page, int size);
    GalleryImageResponse update(UUID id, GalleryImageUpdate update);
    void delete(UUID id);
}
