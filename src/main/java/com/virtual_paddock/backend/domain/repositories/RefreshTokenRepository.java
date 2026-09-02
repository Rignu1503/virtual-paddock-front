package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.RefreshToken;
import com.virtual_paddock.backend.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    int deleteByUser(User user);
}
