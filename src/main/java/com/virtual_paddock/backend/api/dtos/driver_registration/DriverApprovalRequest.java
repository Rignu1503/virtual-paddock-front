package com.virtual_paddock.backend.api.dtos.driver_registration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverApprovalRequest {

    @NotNull(message = "El ID del equipo asignado es requerido para aprobar la inscripción")
    private UUID assignedTeamId;

    @Size(max = 10, message = "El número no puede exceder 10 caracteres")
    private String carNumber;

    @Size(max = 100, message = "El modelo del auto no puede exceder 100 caracteres")
    private String carModel;
}
