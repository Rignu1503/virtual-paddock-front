package com.virtual_paddock.backend.api.dtos.driver;

import com.virtual_paddock.backend.utils.enums.DriverStatus;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverResponse {
    private UUID id;
    private String name;
    private String gamertag;
    private String nationality;
    private String carNumber;
    private String carModel;
    private DriverStatus status;
    private UUID teamId;
    private String teamName;
}
