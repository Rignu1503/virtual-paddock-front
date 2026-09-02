package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.RaceResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceResultRepository extends JpaRepository<RaceResult, Long> {
    Page<RaceResult> findByRaceEventId(Long raceEventId, Pageable pageable);
    List<RaceResult> findByRaceEventIdOrderByPositionAsc(Long raceEventId);
    List<RaceResult> findByRaceEventId(Long raceEventId);
    java.util.Optional<RaceResult> findByRaceEventIdAndDriverId(Long raceEventId, Long driverId);
    void deleteByRaceEventId(Long raceEventId);
    Page<RaceResult> findByDriverId(Long driverId, Pageable pageable);
}
