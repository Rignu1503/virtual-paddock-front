package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.PointsSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PointsSystemRepository extends JpaRepository<PointsSystem, UUID> {
    List<PointsSystem> findByLeagueId(UUID leagueId);
    Page<PointsSystem> findByLeagueId(UUID leagueId, Pageable pageable);
}
