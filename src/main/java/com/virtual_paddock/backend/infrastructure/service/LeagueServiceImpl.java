package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.league.*;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.entities.User;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ILeagueService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.LeagueMapper;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeagueServiceImpl implements ILeagueService {

    private final LeagueRepository leagueRepository;
    private final com.virtual_paddock.backend.domain.repositories.UserRepository userRepository;
    private final LeagueMapper leagueMapper;

    private League find(UUID id) {
        return this.leagueRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("League")));
    }

    @Override
    public LeagueResponse create(LeagueRequest request) {
        // Obtener el usuario autenticado del contexto de seguridad
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User principal)) {
            throw new BadRequestException("Usuario no autenticado");
        }

        User currentUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("User")));
        
        League league = leagueMapper.toEntity(request);
        league.setUser(currentUser); // Asociar la liga al usuario creador
        
        League saved = leagueRepository.save(league);
        return leagueMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "leagues", key = "#id.toString()")
    public LeagueResponse getById(UUID id) {
        League league = find(id);
        return leagueMapper.toResponse(league);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "leaguesBySlug", key = "#slugUrl")
    public LeagueResponse getBySlugUrl(String slugUrl) {
        League league = leagueRepository.findBySlugUrl(slugUrl)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.NotFound(slugUrl)));
        return leagueMapper.toResponse(league);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeagueBasicResponse> getAll(int page, int size) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Page<League> leaguePage;

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User currentUser) {
            if (currentUser.getRole() == com.virtual_paddock.backend.utils.enums.Role.SUPERADMIN) {
                // El Superadmin tiene visibilidad global de todas las ligas
                leaguePage = leagueRepository.findAll(PageRequest.of(page, size));
            } else {
                // El organizador (LEAGUE_ADMIN) solo ve sus propias ligas
                leaguePage = leagueRepository.findByUserId(currentUser.getId(), PageRequest.of(page, size));
            }
        } else {
            // Público / no autenticado (para portales públicos)
            leaguePage = leagueRepository.findAll(PageRequest.of(page, size));
        }

        return PageResponseHelper.fromPage(leaguePage, leagueMapper::toBasicResponse);
    }

    @Override
    @CacheEvict(value = {"leagues", "leaguesBySlug"}, allEntries = true)
    public LeagueResponse update(UUID id, LeagueUpdate update) {
        League league = find(id);
        verifyOwnershipOrSuperadmin(league);
        leagueMapper.updateEntityFromDto(update, league);
        League updated = leagueRepository.save(league);
        return leagueMapper.toResponse(updated);
    }

    @Override
    @CacheEvict(value = {"leagues", "leaguesBySlug"}, allEntries = true)
    public void delete(UUID id) {
        League league = find(id);
        verifyOwnershipOrSuperadmin(league);
        leagueRepository.delete(league);
    }

    private void verifyOwnershipOrSuperadmin(League league) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User currentUser) {
            if (currentUser.getRole() == com.virtual_paddock.backend.utils.enums.Role.SUPERADMIN) {
                return; // Superadmin puede gestionar cualquier liga
            }
            if (league.getUser() != null && league.getUser().getId().equals(currentUser.getId())) {
                return; // El organizador dueño puede gestionar su liga
            }
        }
        throw new BadRequestException("Acceso denegado: No tienes permisos para gestionar esta liga.");
    }
}
