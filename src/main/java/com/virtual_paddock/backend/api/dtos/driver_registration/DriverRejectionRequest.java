package com.virtual_paddock.backend.api.dtos.driver_registration;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRejectionRequest {

    @Size(max = 500, message = "El motivo de rechazo no puede exceder 500 caracteres")
    private String reason;
}
