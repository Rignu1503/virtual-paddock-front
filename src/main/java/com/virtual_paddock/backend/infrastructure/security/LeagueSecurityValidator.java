package com.virtual_paddock.backend.infrastructure.security;

import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.entities.User;
import com.virtual_paddock.backend.utils.enums.Role;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LeagueSecurityValidator {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    public boolean canManageLeague(League league, User currentUser) {
        if (league == null || currentUser == null) {
            return false;
        }
        if (currentUser.getRole() == Role.SUPERADMIN) {
            return true;
        }
        if (league.getUser() != null && league.getUser().getId().equals(currentUser.getId())) {
            return true;
        }
        if (league.getAdministrators() != null &&
            league.getAdministrators().stream().anyMatch(admin -> admin.getId().equals(currentUser.getId()))) {
            return true;
        }
        return false;
    }

    public void verifyOwnershipOrSuperadmin(League league) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("Acceso denegado: Debes iniciar sesión.");
        }
        if (!canManageLeague(league, currentUser)) {
            throw new BadRequestException("Acceso denegado: No tienes permisos para gestionar esta liga.");
        }
    }
}
