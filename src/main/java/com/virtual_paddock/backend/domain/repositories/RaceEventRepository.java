package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.utils.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RaceEventRepository extends JpaRepository<RaceEvent, UUID> {
    Page<RaceEvent> findBySeasonId(UUID seasonId, Pageable pageable);
    List<RaceEvent> findBySeasonId(UUID seasonId);
    Page<RaceEvent> findBySeasonIdAndStatus(UUID seasonId, EventStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"raceResults.driver.team", "sanctions.driver"})
    List<RaceEvent> findWithDetailsBySeasonId(UUID seasonId);
}
