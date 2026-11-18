package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeasonRepository extends JpaRepository<Season, UUID> {
    Page<Season> findByChampionshipId(UUID championshipId, Pageable pageable);
    List<Season> findByChampionshipId(UUID championshipId);

    List<Season> findByChampionshipIdOrderByIsDefaultDesc(UUID championshipId);

    @Modifying
    @Query("UPDATE Season s SET s.isDefault = false WHERE s.championship.id = :championshipId AND s.id != :seasonId")
    void clearDefaultExcept(@Param("championshipId") UUID championshipId, @Param("seasonId") UUID seasonId);

    @Modifying
    @Query("UPDATE Season s SET s.isDefault = false WHERE s.championship.id = :championshipId")
    void clearAllDefaults(@Param("championshipId") UUID championshipId);
}
