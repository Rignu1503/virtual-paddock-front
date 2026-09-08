package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.RaceResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RaceResultRepository extends JpaRepository<RaceResult, UUID> {
    Page<RaceResult> findByRaceEventId(UUID raceEventId, Pageable pageable);
    List<RaceResult> findByRaceEventIdOrderByPositionAsc(UUID raceEventId);
    List<RaceResult> findByRaceEventId(UUID raceEventId);
    Optional<RaceResult> findByRaceEventIdAndDriverId(UUID raceEventId, UUID driverId);

    @Modifying
    @Query("DELETE FROM RaceResult r WHERE r.raceEvent.id = :raceEventId")
    void deleteByRaceEventId(@Param("raceEventId") UUID raceEventId);

    Page<RaceResult> findByDriverId(UUID driverId, Pageable pageable);
}
