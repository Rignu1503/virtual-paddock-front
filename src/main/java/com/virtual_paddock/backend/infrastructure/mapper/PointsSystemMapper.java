package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.pointssystem.*;
import com.virtual_paddock.backend.domain.entities.PointRule;
import com.virtual_paddock.backend.domain.entities.PointsSystem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PointsSystemMapper {

    @Mapping(target = "leagueId", source = "league.id")
    @Mapping(target = "leagueName", source = "league.name")
    PointsSystemResponse toResponse(PointsSystem pointsSystem);

    @Mapping(target = "rulesCount", expression = "java(pointsSystem.getRules() != null ? pointsSystem.getRules().size() : 0)")
    PointsSystemBasicResponse toBasicResponse(PointsSystem pointsSystem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "rules", ignore = true)
    PointsSystem toEntity(PointsSystemRequest request);

    PointRuleDto toRuleDto(PointRule pointRule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pointsSystem", ignore = true)
    PointRule toRuleEntity(PointRuleDto ruleDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "rules", ignore = true)
    void updateEntityFromDto(PointsSystemUpdate update, @MappingTarget PointsSystem pointsSystem);
}
