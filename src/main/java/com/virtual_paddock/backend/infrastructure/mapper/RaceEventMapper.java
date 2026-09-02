package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.raceevent.*;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RaceEventMapper {

    @Mapping(target = "seasonId", source = "season.id")
    RaceEventResponse toResponse(RaceEvent raceEvent);

    RaceEventBasicResponse toBasicResponse(RaceEvent raceEvent);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    @Mapping(target = "raceResults", ignore = true)
    @Mapping(target = "sanctions", ignore = true)
    RaceEvent toEntity(RaceEventRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "season", ignore = true)
    @Mapping(target = "raceResults", ignore = true)
    @Mapping(target = "sanctions", ignore = true)
    void updateEntityFromDto(RaceEventUpdate update, @MappingTarget RaceEvent raceEvent);
}
