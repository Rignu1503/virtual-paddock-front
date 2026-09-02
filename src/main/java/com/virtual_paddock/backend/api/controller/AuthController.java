package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.auth.AuthResponse;
import com.virtual_paddock.backend.api.dtos.auth.LoginRequest;
import com.virtual_paddock.backend.api.dtos.auth.RegisterRequest;
import com.virtual_paddock.backend.domain.entities.RefreshToken;
import com.virtual_paddock.backend.infrastructure.abstract_service.IAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private int refreshTokenDurationMs;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        
        AuthResponse authResponse = authService.register(request);
        
        RefreshToken refreshToken = authService.createRefreshToken(authResponse.getUserId());
        addRefreshTokenCookie(response, refreshToken.getToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario registrado exitosamente", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        
        AuthResponse authResponse = authService.login(request);
        
        RefreshToken refreshToken = authService.createRefreshToken(authResponse.getUserId());
        addRefreshTokenCookie(response, refreshToken.getToken());

        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token no encontrado en las cookies"));
        }

        try {
            AuthResponse authResponse = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.ok("Token refrescado exitosamente", authResponse));
        } catch (Exception e) {
            cleanRefreshTokenCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        String refreshTokenStr = extractRefreshTokenFromCookie(request);
        if (refreshTokenStr != null) {
            try {
                // Para hacer el logout total deberíamos extraer el userId del token JWT o del refreshToken
                // En una app real, podrías obtener el userId del SecurityContextHolder
                // authService.logout(userId);
            } catch (Exception ignored) { }
        }
        
        cleanRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.ok("Logout exitoso", null));
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Requiere HTTPS en producción
        cookie.setPath("/api/auth"); // Solo se envía a endpoints de auth
        cookie.setMaxAge(refreshTokenDurationMs / 1000); // En segundos
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void cleanRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0); // Eliminar cookie
        response.addCookie(cookie);
    }
}
