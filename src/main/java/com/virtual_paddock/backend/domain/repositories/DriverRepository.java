package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.utils.enums.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Page<Driver> findByTeamId(UUID teamId, Pageable pageable);
    Page<Driver> findByTeamLeagueId(UUID leagueId, Pageable pageable);
    Page<Driver> findByStatus(DriverStatus status, Pageable pageable);

    @Query(
        "SELECT d FROM Driver d WHERE (:leagueId IS NULL OR d.team.league.id = :leagueId OR d.team.championship.league.id = :leagueId) " +
        "AND (:championshipId IS NULL OR d.team.championship.id = :championshipId) " +
        "AND (:teamId IS NULL OR d.team.id = :teamId)"
    )
    Page<Driver> findByFilter(
        @Param("leagueId") UUID leagueId,
        @Param("championshipId") UUID championshipId,
        @Param("teamId") UUID teamId,
        Pageable pageable
    );

    @Query(
        "SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END FROM Driver d WHERE " +
        "(d.team.league.id = :leagueId OR d.team.championship.league.id = :leagueId) AND " +
        "LOWER(TRIM(d.name)) = LOWER(TRIM(:name))"
    )
    boolean existsByNameInLeague(
        @Param("leagueId") UUID leagueId,
        @Param("name") String name
    );

    @Query(
        "SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END FROM Driver d WHERE " +
        "(d.team.league.id = :leagueId OR d.team.championship.league.id = :leagueId) AND " +
        "d.gamertag IS NOT NULL AND LOWER(TRIM(d.gamertag)) = LOWER(TRIM(:gamertag))"
    )
    boolean existsByGamertagInLeague(
        @Param("leagueId") UUID leagueId,
        @Param("gamertag") String gamertag
    );

    @Query(
        "SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END FROM Driver d WHERE " +
        "(d.team.league.id = :leagueId OR d.team.championship.league.id = :leagueId) AND " +
        "d.carNumber IS NOT NULL AND " +
        "(LOWER(TRIM(d.carNumber)) = LOWER(TRIM(:cleanCarNumber)) OR " +
        "LOWER(TRIM(d.carNumber)) = LOWER(CONCAT('#', TRIM(:cleanCarNumber))))"
    )
    boolean existsByCarNumberInLeague(
        @Param("leagueId") UUID leagueId,
        @Param("cleanCarNumber") String cleanCarNumber
    );
}
