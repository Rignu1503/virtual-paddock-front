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
}
