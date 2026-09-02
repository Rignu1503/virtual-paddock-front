package com.virtual_paddock.backend.api.dtos.driver;

import com.virtual_paddock.backend.utils.enums.DriverStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverUpdate {
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 100, message = "El gamertag no puede exceder 100 caracteres")
    private String gamertag;

    @Size(max = 50, message = "La nacionalidad no puede exceder 50 caracteres")
    private String nationality;

    @Size(max = 10, message = "El dorsal no puede exceder 10 caracteres")
    private String carNumber;

    @Size(max = 100, message = "El modelo de auto no puede exceder 100 caracteres")
    private String carModel;

    private DriverStatus status;
    private Long teamId;
}
