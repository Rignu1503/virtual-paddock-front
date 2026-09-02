package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.sanction.*;
import com.virtual_paddock.backend.domain.entities.Sanction;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SanctionMapper {

    @Mapping(target = "raceEventId", source = "raceEvent.id")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    SanctionResponse toResponse(Sanction sanction);

    @Mapping(target = "driverName", source = "driver.name")
    SanctionBasicResponse toBasicResponse(Sanction sanction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "raceEvent", ignore = true)
    @Mapping(target = "driver", ignore = true)
    Sanction toEntity(SanctionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "raceEvent", ignore = true)
    @Mapping(target = "driver", ignore = true)
    void updateEntityFromDto(SanctionUpdate update, @MappingTarget Sanction sanction);
}
