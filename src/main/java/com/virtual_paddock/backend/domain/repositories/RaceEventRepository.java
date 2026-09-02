package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.utils.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceEventRepository extends JpaRepository<RaceEvent, Long> {
    Page<RaceEvent> findBySeasonId(Long seasonId, Pageable pageable);
    java.util.List<RaceEvent> findBySeasonId(Long seasonId);
    Page<RaceEvent> findBySeasonIdAndStatus(Long seasonId, EventStatus status, Pageable pageable);
}
