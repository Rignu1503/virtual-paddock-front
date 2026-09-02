package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.raceresult.*;
import java.util.List;
import java.util.Map;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.entities.RaceResult;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.RaceResultRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IRaceResultService;
import com.virtual_paddock.backend.infrastructure.mapper.RaceResultMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RaceResultServiceImpl implements IRaceResultService {

    private final RaceResultRepository raceResultRepository;
    private final RaceEventRepository raceEventRepository;
    private final DriverRepository driverRepository;
    private final RaceResultMapper raceResultMapper;
    private final RaceCalculationService raceCalculationService;

    @Override
    public List<RaceResultResponse> processBatchResults(Long raceEventId, RaceResultBulkRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId)
                .orElseThrow(() -> new EntityNotFoundException("Evento de carrera no encontrado con ID: " + raceEventId));

        // Validar que todos los driverId sean válidos y positivos
        for (RaceResultBulkItemRequest item : request.getResults()) {
            if (item.getDriverId() == null || item.getDriverId() <= 0) {
                throw new IllegalArgumentException("No se pueden guardar resultados con pilotos no emparejados. Por favor empareje al piloto: " 
                        + (item.getDriverName() != null ? item.getDriverName() : "Piloto Desconocido"));
            }
        }

        // Obtener todos los IDs de pilotos involucrados
        List<Long> driverIds = request.getResults().stream()
                .map(RaceResultBulkItemRequest::getDriverId)
                .toList();

        List<Driver> drivers = driverRepository.findAllById(driverIds);
        if (drivers.size() != driverIds.size()) {
            throw new EntityNotFoundException("Uno o más pilotos no fueron encontrados en la base de datos");
        }

        Map<Long, Driver> driversMap = drivers.stream()
                .collect(java.util.stream.Collectors.toMap(Driver::getId, d -> d));

        // Eliminar resultados previos de este evento para reemplazarlos con el cálculo nuevo
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
    public List<RaceResultResponse> previewBatchResults(Long raceEventId, RaceResultBulkRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId)
                .orElseThrow(() -> new EntityNotFoundException("Evento de carrera no encontrado con ID: " + raceEventId));

        List<Long> driverIds = request.getResults().stream()
                .map(RaceResultBulkItemRequest::getDriverId)
                .filter(id -> id != null && id > 0)
                .toList();

        List<Driver> drivers = driverRepository.findAllById(driverIds);
        Map<Long, Driver> driversMap = drivers.stream()
                .collect(java.util.stream.Collectors.toMap(Driver::getId, d -> d));

        // Inyectar pilotos ficticios/mocks para los que no se emparejaron (IDs negativos o nulos)
        for (RaceResultBulkItemRequest item : request.getResults()) {
            if (item.getDriverId() == null || item.getDriverId() <= 0) {
                Long mockId = item.getDriverId() != null ? item.getDriverId() : -System.nanoTime();
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
    public List<RaceResultResponse> recalculateEventStandings(Long raceEventId) {
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId)
                .orElseThrow(() -> new EntityNotFoundException("Evento de carrera no encontrado con ID: " + raceEventId));

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
    public RaceResultResponse create(RaceResultRequest request) {
        RaceEvent raceEvent = raceEventRepository.findById(request.getRaceEventId())
                .orElseThrow(() -> new EntityNotFoundException("Evento de carrera no encontrado con ID: " + request.getRaceEventId()));
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new EntityNotFoundException("Piloto no encontrado con ID: " + request.getDriverId()));

        RaceResult raceResult = raceResultMapper.toEntity(request);
        raceResult.setRaceEvent(raceEvent);
        raceResult.setDriver(driver);
        RaceResult saved = raceResultRepository.save(raceResult);
        return raceResultMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RaceResultResponse getById(Long id) {
        RaceResult raceResult = raceResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resultado no encontrado con ID: " + id));
        return raceResultMapper.toResponse(raceResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RaceResultBasicResponse> getByRaceEventId(Long raceEventId, int page, int size) {
        Page<RaceResult> resultPage = raceResultRepository.findByRaceEventId(raceEventId, PageRequest.of(page, size));
        return buildPageResponse(resultPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RaceResultBasicResponse> getByDriverId(Long driverId, int page, int size) {
        Page<RaceResult> resultPage = raceResultRepository.findByDriverId(driverId, PageRequest.of(page, size));
        return buildPageResponse(resultPage);
    }

    @Override
    public RaceResultResponse update(Long id, RaceResultUpdate update) {
        RaceResult raceResult = raceResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resultado no encontrado con ID: " + id));
        raceResultMapper.updateEntityFromDto(update, raceResult);
        RaceResult updated = raceResultRepository.save(raceResult);
        return raceResultMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!raceResultRepository.existsById(id)) {
            throw new EntityNotFoundException("Resultado no encontrado con ID: " + id);
        }
        raceResultRepository.deleteById(id);
    }

    private PageResponse<RaceResultBasicResponse> buildPageResponse(Page<RaceResult> page) {
        return PageResponse.<RaceResultBasicResponse>builder()
                .content(page.getContent().stream().map(raceResultMapper::toBasicResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
