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
    Page<Team> findByChampionshipId(UUID championshipId, Pageable pageable);
    Page<Team> findBySeasonId(UUID seasonId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Team t WHERE (:leagueId IS NULL OR t.league.id = :leagueId) " +
        "AND (:championshipId IS NULL OR t.championship.id = :championshipId) " +
        "AND (:seasonId IS NULL OR t.season.id = :seasonId)"
    )
    Page<Team> findByFilter(
        @org.springframework.data.repository.query.Param("leagueId") UUID leagueId,
        @org.springframework.data.repository.query.Param("championshipId") UUID championshipId,
        @org.springframework.data.repository.query.Param("seasonId") UUID seasonId,
        Pageable pageable
    );
}
