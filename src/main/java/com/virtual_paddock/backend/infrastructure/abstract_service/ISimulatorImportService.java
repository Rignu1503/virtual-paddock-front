package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ISimulatorImportService {
    RaceResultBulkRequest parseSimulatorFile(MultipartFile file, Long raceEventId);
}
