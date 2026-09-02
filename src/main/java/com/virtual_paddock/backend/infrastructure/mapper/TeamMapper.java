package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.team.*;
import com.virtual_paddock.backend.domain.entities.Team;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(target = "leagueId", source = "league.id")
    TeamResponse toResponse(Team team);

    TeamBasicResponse toBasicResponse(Team team);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "drivers", ignore = true)
    Team toEntity(TeamRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "drivers", ignore = true)
    void updateEntityFromDto(TeamUpdate update, @MappingTarget Team team);
}
