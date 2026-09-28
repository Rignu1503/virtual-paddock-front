package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.DriverRegistration;
import com.virtual_paddock.backend.utils.enums.DriverRegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DriverRegistrationRepository extends JpaRepository<DriverRegistration, UUID> {

    @Query(
        "SELECT r FROM DriverRegistration r WHERE " +
        "(:leagueId IS NULL OR r.league.id = :leagueId) AND " +
        "(:championshipId IS NULL OR r.championship.id = :championshipId) AND " +
        "(:status IS NULL OR r.status = :status) " +
        "ORDER BY r.createdAt DESC"
    )
    Page<DriverRegistration> findByFilter(
        @Param("leagueId") UUID leagueId,
        @Param("championshipId") UUID championshipId,
        @Param("status") DriverRegistrationStatus status,
        Pageable pageable
    );

    long countByLeagueIdAndStatus(UUID leagueId, DriverRegistrationStatus status);

    long countByStatus(DriverRegistrationStatus status);

    @Query(
        "SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM DriverRegistration r WHERE " +
        "r.league.id = :leagueId AND " +
        "r.status IN ('PENDING', 'APPROVED') AND " +
        "LOWER(TRIM(r.name)) = LOWER(TRIM(:name))"
    )
    boolean existsActiveByNameInLeague(
        @Param("leagueId") UUID leagueId,
        @Param("name") String name
    );

    @Query(
        "SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM DriverRegistration r WHERE " +
        "r.league.id = :leagueId AND " +
        "r.status IN ('PENDING', 'APPROVED') AND " +
        "r.gamertag IS NOT NULL AND LOWER(TRIM(r.gamertag)) = LOWER(TRIM(:gamertag))"
    )
    boolean existsActiveByGamertagInLeague(
        @Param("leagueId") UUID leagueId,
        @Param("gamertag") String gamertag
    );

    @Query(
        "SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM DriverRegistration r WHERE " +
        "r.league.id = :leagueId AND " +
        "r.status IN ('PENDING', 'APPROVED') AND " +
        "r.carNumber IS NOT NULL AND " +
        "(LOWER(TRIM(r.carNumber)) = LOWER(TRIM(:cleanCarNumber)) OR " +
        "LOWER(TRIM(r.carNumber)) = LOWER(CONCAT('#', TRIM(:cleanCarNumber))))"
    )
    boolean existsActiveByCarNumberInLeague(
        @Param("leagueId") UUID leagueId,
        @Param("cleanCarNumber") String cleanCarNumber
    );
}
