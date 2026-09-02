package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.raceresult.*;
import com.virtual_paddock.backend.domain.entities.RaceResult;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RaceResultMapper {

    @Mapping(target = "raceEventId", source = "raceEvent.id")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    RaceResultResponse toResponse(RaceResult raceResult);

    @Mapping(target = "driverName", source = "driver.name")
    @Mapping(target = "driverId", source = "driver.id")
    RaceResultBasicResponse toBasicResponse(RaceResult raceResult);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "raceEvent", ignore = true)
    @Mapping(target = "driver", ignore = true)
    RaceResult toEntity(RaceResultRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "raceEvent", ignore = true)
    @Mapping(target = "driver", ignore = true)
    void updateEntityFromDto(RaceResultUpdate update, @MappingTarget RaceResult raceResult);
}
