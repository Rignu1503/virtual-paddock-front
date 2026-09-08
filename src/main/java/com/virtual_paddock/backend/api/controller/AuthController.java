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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private int refreshTokenDurationMs;

    @Value("${application.security.cookie.secure:false}")
    private boolean isSecureCookie;

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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Usuario no autenticado"));
        }
        AuthResponse currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido exitosamente", currentUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        String refreshTokenStr = extractRefreshTokenFromCookie(request);
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            try {
                authService.logoutByToken(refreshTokenStr);
            } catch (Exception ignored) { }
        }
        
        cleanRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.ok("Logout exitoso", null));
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        String sameSite = isSecureCookie ? "None" : "Lax";
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(isSecureCookie)
                .path("/api/auth")
                .maxAge(refreshTokenDurationMs / 1000)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
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
        String sameSite = isSecureCookie ? "None" : "Lax";
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(isSecureCookie)
                .path("/api/auth")
                .maxAge(0)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
