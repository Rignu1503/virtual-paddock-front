package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.raceresult.*;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.RaceResult;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.RaceResultRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceResultService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.RaceResultMapper;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RaceResultServiceImpl implements IRaceResultService {

    private final RaceResultRepository raceResultRepository;
    private final RaceEventRepository raceEventRepository;
    private final DriverRepository driverRepository;
    private final RaceResultMapper raceResultMapper;
    private final RaceCalculationService raceCalculationService;

    private RaceResult find(UUID id) {
        return this.raceResultRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("RaceResult")));
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public List<RaceResultResponse> processBatchResults(UUID raceEventId, RaceResultBulkRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));

        // Validar que todos los driverId sean válidos
        for (RaceResultBulkItemRequest item : request.getResults()) {
            if (item.getDriverId() == null) {
                throw new BadRequestException("No se pueden guardar resultados con pilotos no emparejados. Por favor empareje al piloto: " 
                        + (item.getDriverName() != null ? item.getDriverName() : "Piloto Desconocido"));
            }
        }

        // Obtener todos los IDs de pilotos involucrados
        List<UUID> driverIds = request.getResults().stream()
                .map(RaceResultBulkItemRequest::getDriverId)
                .toList();

        List<Driver> drivers = driverRepository.findAllById(driverIds);
        if (drivers.size() != driverIds.size()) {
            throw new BadRequestException("Uno o más pilotos no fueron encontrados en la base de datos");
        }

        Map<UUID, Driver> driversMap = drivers.stream()
                .collect(Collectors.toMap(Driver::getId, d -> d));

        // Eliminar resultados previos de este evento con consulta masiva eficiente
        raceResultRepository.deleteByRaceEventId(raceEventId);

        // Procesar con el motor de cálculo de carrera (tiempos, penalizaciones, multiclase, puntos)
        List<RaceResult> calculatedResults = raceCalculationService.calculateRaceResults(
                raceEvent,
                request.getResults(),
                driversMap
        );

        List<RaceResult> savedResults = raceResultRepository.saveAll(calculatedResults);
        return savedResults.stream().map(raceResultMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceResultResponse> previewBatchResults(UUID raceEventId, RaceResultBulkRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));

        List<UUID> driverIds = request.getResults().stream()
                .map(RaceResultBulkItemRequest::getDriverId)
                .filter(Objects::nonNull)
                .toList();

        List<Driver> drivers = driverRepository.findAllById(driverIds);
        Map<UUID, Driver> driversMap = drivers.stream()
                .collect(Collectors.toMap(Driver::getId, d -> d));

        // Inyectar pilotos ficticios/mocks para los que no se emparejaron (IDs nulos o no existentes)
        for (RaceResultBulkItemRequest item : request.getResults()) {
            if (item.getDriverId() == null) {
                UUID mockId = UUID.randomUUID();
                item.setDriverId(mockId);
                Driver mockDriver = Driver.builder()
                        .id(mockId)
                        .name(item.getDriverName() != null ? item.getDriverName() : "Piloto No Emparejado")
                        .carNumber(item.getCarNumber())
                        .status(com.virtual_paddock.backend.utils.enums.DriverStatus.ACTIVE)
                        .build();
                driversMap.put(mockId, mockDriver);
            } else if (!driversMap.containsKey(item.getDriverId())) {
                Driver mockDriver = Driver.builder()
                        .id(item.getDriverId())
                        .name(item.getDriverName() != null ? item.getDriverName() : "Piloto #" + item.getDriverId())
                        .carNumber(item.getCarNumber())
                        .status(com.virtual_paddock.backend.utils.enums.DriverStatus.ACTIVE)
                        .build();
                driversMap.put(item.getDriverId(), mockDriver);
            }
        }

        // Calcular en memoria sin guardar en la BD
        List<RaceResult> calculatedResults = raceCalculationService.calculateRaceResults(
                raceEvent,
                request.getResults(),
                driversMap
        );

        return calculatedResults.stream().map(raceResultMapper::toResponse).toList();
    }

    @Override
    public List<RaceResultResponse> recalculateEventStandings(UUID raceEventId) {
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));

        List<RaceResult> currentResults = raceResultRepository.findByRaceEventId(raceEventId);
        if (currentResults.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // Mapear resultados actuales a formato de items bulk para recálculo
        List<RaceResultBulkItemRequest> bulkItems = currentResults.stream().map(r ->
                RaceResultBulkItemRequest.builder()
                        .driverId(r.getDriver().getId())
                        .category(r.getCategory())
                        .totalTime(r.getTotalTime())
                        .penaltiesSeconds(r.getPenaltiesSeconds() != null ? r.getPenaltiesSeconds() : 0)
                        .fastestLap(Boolean.TRUE.equals(r.getFastestLap()))
                        .finishOrder(r.getPosition())
                        .build()
        ).toList();

        RaceResultBulkRequest bulkRequest = RaceResultBulkRequest.builder()
                .results(bulkItems)
                .build();

        return processBatchResults(raceEventId, bulkRequest);
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public RaceResultResponse create(RaceResultRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(request.getRaceEventId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Driver")));

        RaceResult raceResult = raceResultMapper.toEntity(request);
        raceResult.setRaceEvent(raceEvent);
        raceResult.setDriver(driver);
        RaceResult saved = raceResultRepository.save(raceResult);
        return raceResultMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RaceResultResponse getById(UUID id) {
        RaceResult raceResult = find(id);
        return raceResultMapper.toResponse(raceResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RaceResultBasicResponse> getByRaceEventId(UUID raceEventId, int page, int size) {
        Page<RaceResult> resultPage = raceResultRepository.findByRaceEventId(raceEventId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(resultPage, raceResultMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RaceResultBasicResponse> getByDriverId(UUID driverId, int page, int size) {
        Page<RaceResult> resultPage = raceResultRepository.findByDriverId(driverId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(resultPage, raceResultMapper::toBasicResponse);
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public RaceResultResponse update(UUID id, RaceResultUpdate update) {
        RaceResult raceResult = find(id);
        raceResultMapper.updateEntityFromDto(update, raceResult);
        RaceResult updated = raceResultRepository.save(raceResult);
        return raceResultMapper.toResponse(updated);
    }

    @Override
    @CacheEvict(value = {"driverStandings", "teamStandings"}, allEntries = true)
    public void delete(UUID id) {
        RaceResult raceResult = find(id);
        raceResultRepository.delete(raceResult);
    }
}
