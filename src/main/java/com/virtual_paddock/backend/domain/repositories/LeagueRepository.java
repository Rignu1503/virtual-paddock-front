package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {
    Optional<League> findBySlugUrl(String slugUrl);
    boolean existsBySlugUrl(String slugUrl);
}
