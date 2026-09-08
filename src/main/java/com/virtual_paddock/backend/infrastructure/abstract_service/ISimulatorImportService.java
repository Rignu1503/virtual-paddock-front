package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ISimulatorImportService {
    RaceResultBulkRequest parseSimulatorFile(MultipartFile file, UUID raceEventId);
}
