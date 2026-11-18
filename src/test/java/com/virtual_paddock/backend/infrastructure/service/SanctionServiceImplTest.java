package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.sanction.SanctionRequest;
import com.virtual_paddock.backend.api.dtos.sanction.SanctionResponse;
import com.virtual_paddock.backend.api.dtos.sanction.SanctionUpdate;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.RaceResult;
import com.virtual_paddock.backend.domain.entities.Sanction;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.RaceResultRepository;
import com.virtual_paddock.backend.domain.repositories.SanctionRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceResultService;
import com.virtual_paddock.backend.infrastructure.mapper.SanctionMapper;
import com.virtual_paddock.backend.utils.enums.SanctionSeverity;
import com.virtual_paddock.backend.utils.enums.SanctionStatus;
import com.virtual_paddock.backend.utils.enums.SanctionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SanctionServiceImplTest {

    @Mock
    private SanctionRepository sanctionRepository;

    @Mock
    private RaceEventRepository raceEventRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RaceResultRepository raceResultRepository;

    @Mock
    private SanctionMapper sanctionMapper;

    @Mock
    private IRaceResultService raceResultService;

    @InjectMocks
    private SanctionServiceImpl sanctionService;

    private UUID raceEventId;
    private UUID driverId;
    private RaceEvent raceEvent;
    private Driver driver;
    private RaceResult raceResult;

    @BeforeEach
    void setUp() {
        raceEventId = UUID.randomUUID();
        driverId = UUID.randomUUID();

        raceEvent = new RaceEvent();
        raceEvent.setId(raceEventId);

        driver = new Driver();
        driver.setId(driverId);
        driver.setName("Max Verstappen");

        raceResult = new RaceResult();
        raceResult.setId(UUID.randomUUID());
        raceResult.setRaceEvent(raceEvent);
        raceResult.setDriver(driver);
        raceResult.setStatus("FINISHED");
        raceResult.setPoints(25);
        raceResult.setPenaltiesSeconds(0);
    }

    @Test
    void create_whenDisqualificationSeverity_atomicallySetsStatusDQAndZeroPointsAndRecalculates() {
        SanctionRequest request = SanctionRequest.builder()
                .raceEventId(raceEventId)
                .driverId(driverId)
                .type(SanctionType.CONDUCTA)
                .severity(SanctionSeverity.DESCALIFICACION)
                .status(SanctionStatus.APLICADO)
                .detail("Ignorar banderas negras repetidamente")
                .build();

        Sanction sanction = Sanction.builder()
                .raceEvent(raceEvent)
                .driver(driver)
                .type(SanctionType.CONDUCTA)
                .severity(SanctionSeverity.DESCALIFICACION)
                .status(SanctionStatus.APLICADO)
                .detail("Ignorar banderas negras repetidamente")
                .build();

        when(raceEventRepository.findById(raceEventId)).thenReturn(Optional.of(raceEvent));
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(sanctionMapper.toEntity(request)).thenReturn(sanction);
        when(sanctionRepository.save(sanction)).thenReturn(sanction);
        when(raceResultRepository.findByRaceEventIdAndDriverId(raceEventId, driverId)).thenReturn(Optional.of(raceResult));
        when(sanctionMapper.toResponse(sanction)).thenReturn(SanctionResponse.builder().id(UUID.randomUUID()).build());

        SanctionResponse response = sanctionService.create(request);

        assertNotNull(response);
        assertEquals("DQ", raceResult.getStatus());
        assertEquals(0, raceResult.getPoints());
        verify(raceResultRepository).save(raceResult);
        verify(raceResultService).recalculateEventStandings(raceEventId);
    }

    @Test
    void create_whenDisqualificationDetail_atomicallySetsStatusDQAndRecalculates() {
        SanctionRequest request = SanctionRequest.builder()
                .raceEventId(raceEventId)
                .driverId(driverId)
                .type(SanctionType.COLISION)
                .severity(SanctionSeverity.GRAVE)
                .status(SanctionStatus.APLICADO)
                .detail("Accidente grave en T1 - DSQ de la carrera")
                .build();

        Sanction sanction = Sanction.builder()
                .raceEvent(raceEvent)
                .driver(driver)
                .type(SanctionType.COLISION)
                .severity(SanctionSeverity.GRAVE)
                .status(SanctionStatus.APLICADO)
                .detail("Accidente grave en T1 - DSQ de la carrera")
                .build();

        when(raceEventRepository.findById(raceEventId)).thenReturn(Optional.of(raceEvent));
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(sanctionMapper.toEntity(request)).thenReturn(sanction);
        when(sanctionRepository.save(sanction)).thenReturn(sanction);
        when(raceResultRepository.findByRaceEventIdAndDriverId(raceEventId, driverId)).thenReturn(Optional.of(raceResult));
        when(sanctionMapper.toResponse(sanction)).thenReturn(SanctionResponse.builder().id(UUID.randomUUID()).build());

        SanctionResponse response = sanctionService.create(request);

        assertNotNull(response);
        assertEquals("DQ", raceResult.getStatus());
        assertEquals(0, raceResult.getPoints());
        verify(raceResultRepository).save(raceResult);
        verify(raceResultService).recalculateEventStandings(raceEventId);
    }

    @Test
    void create_whenPenaltySeconds_updatesPenaltiesAndRecalculates() {
        SanctionRequest request = SanctionRequest.builder()
                .raceEventId(raceEventId)
                .driverId(driverId)
                .type(SanctionType.TRACK_LIMITS)
                .severity(SanctionSeverity.LEVE)
                .status(SanctionStatus.APLICADO)
                .penaltySeconds(5)
                .build();

        Sanction sanction = Sanction.builder()
                .raceEvent(raceEvent)
                .driver(driver)
                .type(SanctionType.TRACK_LIMITS)
                .severity(SanctionSeverity.LEVE)
                .status(SanctionStatus.APLICADO)
                .penaltySeconds(5)
                .build();

        raceResult.setPenaltiesSeconds(5); // Ya tenía 5s previos

        when(raceEventRepository.findById(raceEventId)).thenReturn(Optional.of(raceEvent));
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(sanctionMapper.toEntity(request)).thenReturn(sanction);
        when(sanctionRepository.save(sanction)).thenReturn(sanction);
        when(raceResultRepository.findByRaceEventIdAndDriverId(raceEventId, driverId)).thenReturn(Optional.of(raceResult));
        when(sanctionMapper.toResponse(sanction)).thenReturn(SanctionResponse.builder().id(UUID.randomUUID()).build());

        SanctionResponse response = sanctionService.create(request);

        assertNotNull(response);
        assertEquals(10, raceResult.getPenaltiesSeconds());
        assertEquals("FINISHED", raceResult.getStatus());
        verify(raceResultRepository).save(raceResult);
        verify(raceResultService).recalculateEventStandings(raceEventId);
    }

    @Test
    void update_whenDisqualification_atomicallySetsStatusDQAndRecalculates() {
        UUID sanctionId = UUID.randomUUID();
        Sanction existingSanction = Sanction.builder()
                .id(sanctionId)
                .raceEvent(raceEvent)
                .driver(driver)
                .type(SanctionType.CONDUCTA)
                .severity(SanctionSeverity.GRAVE)
                .status(SanctionStatus.REVISION)
                .penaltySeconds(0)
                .build();

        SanctionUpdate update = SanctionUpdate.builder()
                .severity(SanctionSeverity.DISQUALIFICATION)
                .status(SanctionStatus.APLICADO)
                .detail("Comisarios confirman DISQUALIFICATION tras apelacion")
                .build();

        when(sanctionRepository.findById(sanctionId)).thenReturn(Optional.of(existingSanction));
        doAnswer(inv -> {
            existingSanction.setSeverity(SanctionSeverity.DISQUALIFICATION);
            existingSanction.setStatus(SanctionStatus.APLICADO);
            existingSanction.setDetail(update.getDetail());
            return null;
        }).when(sanctionMapper).updateEntityFromDto(update, existingSanction);
        when(sanctionRepository.save(existingSanction)).thenReturn(existingSanction);
        when(raceResultRepository.findByRaceEventIdAndDriverId(raceEventId, driverId)).thenReturn(Optional.of(raceResult));
        when(sanctionMapper.toResponse(existingSanction)).thenReturn(SanctionResponse.builder().id(sanctionId).build());

        SanctionResponse response = sanctionService.update(sanctionId, update);

        assertNotNull(response);
        assertEquals("DQ", raceResult.getStatus());
        assertEquals(0, raceResult.getPoints());
        verify(raceResultRepository).save(raceResult);
        verify(raceResultService).recalculateEventStandings(raceEventId);
    }
}
