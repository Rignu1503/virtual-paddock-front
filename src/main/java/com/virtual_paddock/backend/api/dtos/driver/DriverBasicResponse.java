package com.virtual_paddock.backend.api.dtos.driver;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverBasicResponse {
    private UUID id;
    private String name;
    private String gamertag;
    private String nationality;
    private String carNumber;
    private String carModel;
    private String teamName;
}
