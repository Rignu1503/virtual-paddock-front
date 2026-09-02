package com.virtual_paddock.backend.api.dtos.driver;

import com.virtual_paddock.backend.utils.enums.DriverStatus;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverResponse {
    private Long id;
    private String name;
    private String gamertag;
    private String nationality;
    private String carNumber;
    private String carModel;
    private DriverStatus status;
    private Long teamId;
    private String teamName;
}
