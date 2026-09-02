package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.utils.enums.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Page<Driver> findByTeamId(Long teamId, Pageable pageable);
    Page<Driver> findByTeamLeagueId(Long leagueId, Pageable pageable);
    Page<Driver> findByStatus(DriverStatus status, Pageable pageable);
}
