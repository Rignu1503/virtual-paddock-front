package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.IncidentClaim;
import com.virtual_paddock.backend.utils.enums.IncidentClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentClaimRepository extends JpaRepository<IncidentClaim, UUID> {

    Page<IncidentClaim> findByRaceEventId(UUID raceEventId, Pageable pageable);

    List<IncidentClaim> findByRaceEventIdOrderByCreatedAtDesc(UUID raceEventId);

    Page<IncidentClaim> findByStatus(IncidentClaimStatus status, Pageable pageable);

    @Query("SELECT c FROM IncidentClaim c WHERE " +
           "(:raceEventId IS NULL OR c.raceEvent.id = :raceEventId) AND " +
           "(:status IS NULL OR c.status = :status) " +
           "ORDER BY c.createdAt DESC")
    Page<IncidentClaim> findByFilter(
            @Param("raceEventId") UUID raceEventId,
            @Param("status") IncidentClaimStatus status,
            Pageable pageable
    );

    boolean existsByTicketNumber(String ticketNumber);

    long countByRaceEventId(UUID raceEventId);
}
