package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkItemRequest;
import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkRequest;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.RaceEvent;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.ISimulatorImportService;
import com.virtual_paddock.backend.infrastructure.helper.simulator.FileEncodingHelper;
import com.virtual_paddock.backend.infrastructure.helper.simulator.ISimulatorLogParser;
import com.virtual_paddock.backend.infrastructure.helper.simulator.LapTimeHelper;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimulatorImportServiceImpl implements ISimulatorImportService {

    private final RaceEventRepository raceEventRepository;
    private final DriverRepository driverRepository;
    private final List<ISimulatorLogParser> parsers;

    private RaceEvent find(UUID id) {
        return this.raceEventRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("RaceEvent")));
    }

    @Override
    public RaceResultBulkRequest parseSimulatorFile(MultipartFile file, UUID raceEventId) {
        RaceEvent raceEvent = find(raceEventId);

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<Driver> allDrivers = driverRepository.findAll();

        List<RaceResultBulkItemRequest> items;

        try {
            byte[] fileBytes = file.getBytes();
            String content = FileEncodingHelper.decodeUniversalString(fileBytes).trim();

            ISimulatorLogParser parser = parsers.stream()
                    .filter(p -> p.supports(filename, content))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Formato de simulador no reconocido o no soportado: " + filename));

            items = parser.parse(content, allDrivers);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al procesar el archivo de resultados: " + e.getMessage(), e);
        }

        LapTimeHelper.resolveFastestLap(items);

        return RaceResultBulkRequest.builder()
                .results(items)
                .build();
    }
}
