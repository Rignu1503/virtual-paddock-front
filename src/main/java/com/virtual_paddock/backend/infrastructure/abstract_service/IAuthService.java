package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.auth.AuthResponse;
import com.virtual_paddock.backend.api.dtos.auth.LoginRequest;
import com.virtual_paddock.backend.api.dtos.auth.RegisterRequest;
import com.virtual_paddock.backend.domain.entities.RefreshToken;

import java.util.UUID;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshTokenStr);
    void logout(UUID userId);
    void logoutByToken(String refreshTokenStr);
    AuthResponse getCurrentUser(String email);
    RefreshToken createRefreshToken(UUID userId);
}
