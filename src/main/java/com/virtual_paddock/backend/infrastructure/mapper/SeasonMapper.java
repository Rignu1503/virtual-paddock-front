package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.season.*;
import com.virtual_paddock.backend.domain.entities.Season;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeasonMapper {

    @Mapping(target = "championshipId", source = "championship.id")
    SeasonResponse toResponse(Season season);

    SeasonBasicResponse toBasicResponse(Season season);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "championship", ignore = true)
    @Mapping(target = "raceEvents", ignore = true)
    Season toEntity(SeasonRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "championship", ignore = true)
    @Mapping(target = "raceEvents", ignore = true)
    void updateEntityFromDto(SeasonUpdate update, @MappingTarget Season season);
}
