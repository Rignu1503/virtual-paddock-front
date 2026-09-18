package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.driver.*;
import com.virtual_paddock.backend.domain.entities.Driver;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "championshipId", source = "team.championship.id")
    @Mapping(target = "championshipName", source = "team.championship.gameName")
    @Mapping(target = "leagueId", source = "team.league.id")
    DriverResponse toResponse(Driver driver);

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "championshipId", source = "team.championship.id")
    @Mapping(target = "championshipName", source = "team.championship.gameName")
    @Mapping(target = "leagueId", source = "team.league.id")
    DriverBasicResponse toBasicResponse(Driver driver);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "raceResults", ignore = true)
    @Mapping(target = "sanctions", ignore = true)
    Driver toEntity(DriverRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "raceResults", ignore = true)
    @Mapping(target = "sanctions", ignore = true)
    void updateEntityFromDto(DriverUpdate update, @MappingTarget Driver driver);
}
