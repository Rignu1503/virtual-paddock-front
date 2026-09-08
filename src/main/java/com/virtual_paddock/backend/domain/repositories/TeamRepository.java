package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Page<Team> findByLeagueId(UUID leagueId, Pageable pageable);
}
