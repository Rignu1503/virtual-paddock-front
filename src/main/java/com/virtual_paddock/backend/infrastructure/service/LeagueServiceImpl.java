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
    private final com.virtual_paddock.backend.infrastructure.security.LeagueSecurityValidator leagueSecurityValidator;

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
        league.setInviteCode(generateUniqueInviteCode());
        
        League saved = leagueRepository.save(league);
        return leagueMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LeagueResponse getById(UUID id) {
        League league = find(id);
        if (league.getInviteCode() == null || league.getInviteCode().trim().isEmpty()) {
            league.setInviteCode(generateUniqueInviteCode());
            league = leagueRepository.save(league);
        }
        LeagueResponse resp = leagueMapper.toResponse(league);
        User currentUser = leagueSecurityValidator.getCurrentUser();
        if (!leagueSecurityValidator.canManageLeague(league, currentUser)) {
            resp.setInviteCode(null);
        }
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueResponse getBySlugUrl(String slugUrl) {
        League league = leagueRepository.findBySlugUrl(slugUrl)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.NotFound(slugUrl)));
        LeagueResponse resp = leagueMapper.toResponse(league);
        User currentUser = leagueSecurityValidator.getCurrentUser();
        if (!leagueSecurityValidator.canManageLeague(league, currentUser)) {
            resp.setInviteCode(null);
        }
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeagueBasicResponse> getAll(int page, int size) {
        User currentUser = leagueSecurityValidator.getCurrentUser();
        Page<League> leaguePage;

        if (currentUser != null) {
            if (currentUser.getRole() == com.virtual_paddock.backend.utils.enums.Role.SUPERADMIN) {
                // El Superadmin tiene visibilidad global de todas las ligas
                leaguePage = leagueRepository.findAll(PageRequest.of(page, size));
            } else {
                // El organizador (LEAGUE_ADMIN) ve ÚNICAMENTE sus ligas creadas y aquellas donde es co-administrador
                leaguePage = leagueRepository.findByAdministeredUser(currentUser.getId(), PageRequest.of(page, size));
            }
        } else {
            // Público / no autenticado (para portales públicos)
            leaguePage = leagueRepository.findAll(PageRequest.of(page, size));
        }

        return PageResponseHelper.fromPage(leaguePage, league -> {
            LeagueBasicResponse resp = leagueMapper.toBasicResponse(league);
            if (!leagueSecurityValidator.canManageLeague(league, currentUser)) {
                resp.setInviteCode(null);
            }
            return resp;
        });
    }

    @Override
    @CacheEvict(value = {"leagues", "leaguesBySlug"}, allEntries = true)
    public LeagueResponse update(UUID id, LeagueUpdate update) {
        League league = find(id);
        leagueSecurityValidator.verifyOwnershipOrSuperadmin(league);
        leagueMapper.updateEntityFromDto(update, league);
        League updated = leagueRepository.save(league);
        return leagueMapper.toResponse(updated);
    }

    @Override
    @CacheEvict(value = {"leagues", "leaguesBySlug"}, allEntries = true)
    public void delete(UUID id) {
        League league = find(id);
        leagueSecurityValidator.verifyOwnershipOrSuperadmin(league);
        leagueRepository.delete(league);
    }

    @Override
    @CacheEvict(value = {"leagues", "leaguesBySlug"}, allEntries = true)
    public String regenerateInviteCode(UUID id) {
        League league = find(id);
        leagueSecurityValidator.verifyOwnershipOrSuperadmin(league);
        String newCode = generateUniqueInviteCode();
        league.setInviteCode(newCode);
        leagueRepository.save(league);
        return newCode;
    }

    private String generateUniqueInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder("VP-");
        java.security.SecureRandom random = new java.security.SecureRandom();
        String code;
        do {
            sb.setLength(3);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (leagueRepository.existsByInviteCode(code));
        return code;
    }
}
