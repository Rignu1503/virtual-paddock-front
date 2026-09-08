package com.virtual_paddock.backend.domain.repositories;

import com.virtual_paddock.backend.domain.entities.PointRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PointRuleRepository extends JpaRepository<PointRule, UUID> {
    List<PointRule> findByPointsSystemIdOrderByPositionAsc(UUID pointsSystemId);
    void deleteByPointsSystemId(UUID pointsSystemId);
}
