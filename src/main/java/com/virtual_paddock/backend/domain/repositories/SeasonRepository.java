package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    Page<Season> findByChampionshipId(Long championshipId, Pageable pageable);
}
