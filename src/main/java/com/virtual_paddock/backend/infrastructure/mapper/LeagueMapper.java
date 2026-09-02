package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.league.*;
import com.virtual_paddock.backend.domain.entities.League;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LeagueMapper {

    @Mapping(target = "userId", source = "user.id")
    LeagueResponse toResponse(League league);

    LeagueBasicResponse toBasicResponse(League league);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "championships", ignore = true)
    @Mapping(target = "teams", ignore = true)
    League toEntity(LeagueRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "championships", ignore = true)
    @Mapping(target = "teams", ignore = true)
    void updateEntityFromDto(LeagueUpdate update, @MappingTarget League league);
}
