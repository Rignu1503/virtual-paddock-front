package com.virtual_paddock.backend.infrastructure.abstract_service;

import com.fasterxml.jackson.databind.JsonNode;

public interface ILiveTimingService {
    JsonNode getLiveTimingSnapshot();
    byte[] getTrackMapImage();
}
