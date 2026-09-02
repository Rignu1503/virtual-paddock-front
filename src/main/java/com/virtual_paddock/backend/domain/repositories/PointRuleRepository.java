package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.PointRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRuleRepository extends JpaRepository<PointRule, Long> {
    List<PointRule> findByPointsSystemIdOrderByPositionAsc(Long pointsSystemId);
    void deleteByPointsSystemId(Long pointsSystemId);
}
