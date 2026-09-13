package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.League;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeagueRepository extends JpaRepository<League, UUID> {
    Optional<League> findBySlugUrl(String slugUrl);
    boolean existsBySlugUrl(String slugUrl);
    Page<League> findByUserId(UUID userId, Pageable pageable);
    List<League> findByUserId(UUID userId);

    Optional<League> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);

    @Query("SELECT DISTINCT l FROM League l LEFT JOIN l.administrators a WHERE l.user.id = :userId OR a.id = :userId")
    Page<League> findByAdministeredUser(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT DISTINCT l FROM League l LEFT JOIN l.administrators a WHERE l.user.id = :userId OR a.id = :userId")
    List<League> findByAdministeredUser(@Param("userId") UUID userId);
}
