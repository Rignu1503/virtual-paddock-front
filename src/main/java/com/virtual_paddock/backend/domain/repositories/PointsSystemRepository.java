package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.PointsSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsSystemRepository extends JpaRepository<PointsSystem, Long> {
    List<PointsSystem> findByLeagueId(Long leagueId);
    Page<PointsSystem> findByLeagueId(Long leagueId, Pageable pageable);
}
