package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.league.*;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ILeagueService;
import com.virtual_paddock.backend.infrastructure.mapper.LeagueMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LeagueServiceImpl implements ILeagueService {

    private final LeagueRepository leagueRepository;
    private final LeagueMapper leagueMapper;

    @Override
    public LeagueResponse create(LeagueRequest request) {
        // Obtener el usuario autenticado del contexto de seguridad
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        League league = leagueMapper.toEntity(request);
        league.setUser(currentUser); // Asociar la liga al usuario creador
        
        League saved = leagueRepository.save(league);
        return leagueMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueResponse getById(Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Liga no encontrada con ID: " + id));
        return leagueMapper.toResponse(league);
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueResponse getBySlugUrl(String slugUrl) {
        League league = leagueRepository.findBySlugUrl(slugUrl)
                .orElseThrow(() -> new EntityNotFoundException("Liga no encontrada con slug: " + slugUrl));
        return leagueMapper.toResponse(league);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeagueBasicResponse> getAll(int page, int size) {
        Page<League> leaguePage = leagueRepository.findAll(PageRequest.of(page, size));
        return buildPageResponse(leaguePage);
    }

    @Override
    public LeagueResponse update(Long id, LeagueUpdate update) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Liga no encontrada con ID: " + id));
        leagueMapper.updateEntityFromDto(update, league);
        League updated = leagueRepository.save(league);
        return leagueMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!leagueRepository.existsById(id)) {
            throw new EntityNotFoundException("Liga no encontrada con ID: " + id);
        }
        leagueRepository.deleteById(id);
    }

    private PageResponse<LeagueBasicResponse> buildPageResponse(Page<League> page) {
        return PageResponse.<LeagueBasicResponse>builder()
                .content(page.getContent().stream().map(leagueMapper::toBasicResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
