package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.auth.AuthResponse;
import com.virtual_paddock.backend.domain.entities.User;
import com.virtual_paddock.backend.domain.repositories.UserRepository;
import com.virtual_paddock.backend.infrastructure.security.JwtService;
import com.virtual_paddock.backend.infrastructure.security.RefreshTokenService;
import com.virtual_paddock.backend.utils.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@virtualpaddock.gg")
                .password("encoded_pass")
                .role(Role.SUPERADMIN)
                .build();
    }

    @Test
    void getCurrentUser_WhenUserExists_ReturnsAuthResponse() {
        when(userRepository.findByEmail("admin@virtualpaddock.gg")).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.getCurrentUser("admin@virtualpaddock.gg");

        assertNotNull(response);
        assertEquals(sampleUser.getId(), response.getUserId());
        assertEquals("admin@virtualpaddock.gg", response.getEmail());
        assertEquals(Role.SUPERADMIN, response.getRole());
        verify(userRepository).findByEmail("admin@virtualpaddock.gg");
    }

    @Test
    void getCurrentUser_WhenUserDoesNotExist_ThrowsIllegalArgumentException() {
        when(userRepository.findByEmail("unknown@virtualpaddock.gg")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.getCurrentUser("unknown@virtualpaddock.gg"));
    }

    @Test
    void logoutByToken_WhenTokenProvided_CallsRefreshTokenService() {
        String token = "sample-refresh-uuid-token";

        authService.logoutByToken(token);

        verify(refreshTokenService).deleteByToken(token);
    }

    @Test
    void logoutByToken_WhenTokenNullOrBlank_DoesNotCallRefreshTokenService() {
        authService.logoutByToken(null);
        authService.logoutByToken("   ");

        verify(refreshTokenService, never()).deleteByToken(anyString());
    }
}
