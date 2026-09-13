package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.team.*;
import com.virtual_paddock.backend.domain.entities.Team;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(target = "leagueId", source = "league.id")
    @Mapping(target = "championshipId", source = "championship.id")
    @Mapping(target = "championshipName", source = "championship.gameName")
    @Mapping(target = "seasonId", source = "season.id")
    @Mapping(target = "seasonName", source = "season.seasonName")
    TeamResponse toResponse(Team team);

    @Mapping(target = "leagueId", source = "league.id")
    @Mapping(target = "championshipId", source = "championship.id")
    @Mapping(target = "championshipName", source = "championship.gameName")
    @Mapping(target = "seasonId", source = "season.id")
    @Mapping(target = "seasonName", source = "season.seasonName")
    TeamBasicResponse toBasicResponse(Team team);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "championship", ignore = true)
    @Mapping(target = "season", ignore = true)
    @Mapping(target = "drivers", ignore = true)
    Team toEntity(TeamRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "championship", ignore = true)
    @Mapping(target = "season", ignore = true)
    @Mapping(target = "drivers", ignore = true)
    void updateEntityFromDto(TeamUpdate update, @MappingTarget Team team);
}
