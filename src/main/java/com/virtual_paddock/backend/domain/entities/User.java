package com.virtual_paddock.backend.domain.entities;

import com.virtual_paddock.backend.utils.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<League> leagues = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email; // En nuestro sistema el email es el username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Por ahora no implementamos expiración de cuenta
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Por ahora no implementamos bloqueo
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Por ahora no implementamos expiración de credenciales
    }

    @Override
    public boolean isEnabled() {
        return true; // Por ahora todos los usuarios están activos por defecto
    }
}
