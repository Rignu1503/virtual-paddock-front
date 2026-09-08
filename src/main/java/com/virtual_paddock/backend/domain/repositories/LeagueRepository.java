package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.League;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
