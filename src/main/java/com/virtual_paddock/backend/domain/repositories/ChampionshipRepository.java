package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.Championship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChampionshipRepository extends JpaRepository<Championship, Long> {
    Page<Championship> findByLeagueId(Long leagueId, Pageable pageable);
}
