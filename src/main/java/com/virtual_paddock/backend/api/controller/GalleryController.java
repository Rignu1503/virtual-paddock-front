package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.gallery.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IGalleryImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final IGalleryImageService galleryImageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<GalleryImageResponse>> create(@Valid @RequestBody GalleryImageRequest request) {
        GalleryImageResponse response = galleryImageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Imagen agregada exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GalleryImageResponse>> getById(@PathVariable UUID id) {
        GalleryImageResponse response = galleryImageService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<ApiResponse<PageResponse<GalleryImageBasicResponse>>> getByLeagueId(
            @PathVariable UUID leagueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<GalleryImageBasicResponse> response = galleryImageService.getByLeagueId(leagueId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<GalleryImageResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody GalleryImageUpdate update) {
        GalleryImageResponse response = galleryImageService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Imagen actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'LEAGUE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        galleryImageService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Imagen eliminada exitosamente", null));
    }
}
