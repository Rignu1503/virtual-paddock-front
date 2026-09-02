package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.auth.AuthResponse;
import com.virtual_paddock.backend.api.dtos.auth.LoginRequest;
import com.virtual_paddock.backend.api.dtos.auth.RegisterRequest;
import com.virtual_paddock.backend.domain.entities.RefreshToken;
import com.virtual_paddock.backend.domain.entities.User;
import com.virtual_paddock.backend.domain.repositories.UserRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IAuthService;
import com.virtual_paddock.backend.infrastructure.security.JwtService;
import com.virtual_paddock.backend.infrastructure.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .userId(user.getId())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token no está en base de datos!"));
    }

    @Override
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        return refreshTokenService.createRefreshToken(userId);
    }
}
