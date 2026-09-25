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
}
