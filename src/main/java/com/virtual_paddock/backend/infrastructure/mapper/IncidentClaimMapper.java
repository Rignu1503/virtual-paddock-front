package com.virtual_paddock.backend.infrastructure.mapper;

import com.virtual_paddock.backend.api.dtos.incident_claim.IncidentClaimResponse;
import com.virtual_paddock.backend.domain.entities.IncidentClaim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidentClaimMapper {

    @Mapping(target = "raceEventId", source = "raceEvent.id")
    @Mapping(target = "raceName", source = "raceEvent.circuitName")
    @Mapping(target = "roundNumber", source = "raceEvent.roundNumber")
    @Mapping(target = "championshipId", source = "raceEvent.season.championship.id")
    @Mapping(target = "championshipName", source = "raceEvent.season.championship.gameName")
    @Mapping(target = "leagueId", source = "raceEvent.season.championship.league.id")
    @Mapping(target = "reportedDriverId", source = "reportedDriver.id")
    @Mapping(target = "sanctionId", source = "sanction.id")
    IncidentClaimResponse toResponse(IncidentClaim claim);
}
