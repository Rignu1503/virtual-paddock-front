package com.virtual_paddock.backend.infrastructure.helper.simulator;

import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkItemRequest;
import com.virtual_paddock.backend.domain.entities.Driver;

import java.util.List;

public interface ISimulatorLogParser {

    /**
     * Determina si este parser soporta el archivo provisto según nombre o contenido.
     */
    boolean supports(String filename, String content);

    /**
     * Procesa y extrae la lista de resultados de carrera a partir del contenido del log.
     */
    List<RaceResultBulkItemRequest> parse(String content, List<Driver> allDrivers) throws Exception;
}
