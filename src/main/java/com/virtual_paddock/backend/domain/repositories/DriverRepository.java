package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.utils.enums.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Page<Driver> findByTeamId(UUID teamId, Pageable pageable);
    Page<Driver> findByTeamLeagueId(UUID leagueId, Pageable pageable);
    Page<Driver> findByStatus(DriverStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
        "SELECT d FROM Driver d WHERE (:leagueId IS NULL OR d.team.league.id = :leagueId OR d.team.championship.league.id = :leagueId) " +
        "AND (:championshipId IS NULL OR d.team.championship.id = :championshipId) " +
        "AND (:teamId IS NULL OR d.team.id = :teamId)"
    )
    Page<Driver> findByFilter(
        @org.springframework.data.repository.query.Param("leagueId") UUID leagueId,
        @org.springframework.data.repository.query.Param("championshipId") UUID championshipId,
        @org.springframework.data.repository.query.Param("teamId") UUID teamId,
        Pageable pageable
    );
}
