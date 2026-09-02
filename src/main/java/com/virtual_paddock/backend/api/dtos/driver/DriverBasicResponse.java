package com.virtual_paddock.backend.api.dtos.driver;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverBasicResponse {
    private Long id;
    private String name;
    private String gamertag;
    private String carNumber;
}
