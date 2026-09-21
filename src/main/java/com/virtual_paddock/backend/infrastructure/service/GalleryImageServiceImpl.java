package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.gallery.*;
import com.virtual_paddock.backend.domain.entities.GalleryImage;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.repositories.GalleryImageRepository;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IGalleryImageService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.GalleryImageMapper;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GalleryImageServiceImpl implements IGalleryImageService {

    private final GalleryImageRepository galleryImageRepository;
    private final LeagueRepository leagueRepository;
    private final GalleryImageMapper galleryImageMapper;

    private GalleryImage find(UUID id) {
        return this.galleryImageRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("GalleryImage")));
    }

    @Override
    public GalleryImageResponse create(GalleryImageRequest request) {
        GalleryImage image = galleryImageMapper.toEntity(request);

        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("League")));
        image.setLeague(league);

        if (image.getDisplayOrder() == null) {
            image.setDisplayOrder(0);
        }

        GalleryImage saved = galleryImageRepository.save(image);
        return galleryImageMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GalleryImageResponse getById(UUID id) {
        GalleryImage image = find(id);
        return galleryImageMapper.toResponse(image);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GalleryImageBasicResponse> getByLeagueId(UUID leagueId, int page, int size) {
        Page<GalleryImage> imagePage = galleryImageRepository
                .findByLeagueIdOrderByDisplayOrderAscCreatedAtDesc(leagueId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(imagePage, galleryImageMapper::toBasicResponse);
    }

    @Override
    public GalleryImageResponse update(UUID id, GalleryImageUpdate update) {
        GalleryImage image = find(id);
        galleryImageMapper.updateEntityFromDto(update, image);
        GalleryImage updated = galleryImageRepository.save(image);
        return galleryImageMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        GalleryImage image = find(id);
        galleryImageRepository.delete(image);
    }
}
