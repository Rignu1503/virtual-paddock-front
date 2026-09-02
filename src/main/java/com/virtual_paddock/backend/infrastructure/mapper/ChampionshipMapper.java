package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.championship.*;
import com.virtual_paddock.backend.domain.entities.Championship;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ChampionshipMapper {

    @Mapping(target = "leagueId", source = "league.id")
    @Mapping(target = "leagueName", source = "league.name")
    @Mapping(target = "pointsSystemId", source = "pointsSystemRef.id")
    @Mapping(target = "pointsSystemName", source = "pointsSystemRef.name")
    @Mapping(target = "sprintPointsSystemId", source = "sprintPointsSystemRef.id")
    @Mapping(target = "sprintPointsSystemName", source = "sprintPointsSystemRef.name")
    ChampionshipResponse toResponse(Championship championship);

    ChampionshipBasicResponse toBasicResponse(Championship championship);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "pointsSystemRef", ignore = true)
    @Mapping(target = "sprintPointsSystemRef", ignore = true)
    @Mapping(target = "seasons", ignore = true)
    Championship toEntity(ChampionshipRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "pointsSystemRef", ignore = true)
    @Mapping(target = "sprintPointsSystemRef", ignore = true)
    @Mapping(target = "seasons", ignore = true)
    void updateEntityFromDto(ChampionshipUpdate update, @MappingTarget Championship championship);
}
