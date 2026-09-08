package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.Sanction;
import com.virtual_paddock.backend.utils.enums.SanctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SanctionRepository extends JpaRepository<Sanction, UUID> {
    Page<Sanction> findByRaceEventId(UUID raceEventId, Pageable pageable);
    Page<Sanction> findByDriverId(UUID driverId, Pageable pageable);
    Page<Sanction> findByStatus(SanctionStatus status, Pageable pageable);
}
