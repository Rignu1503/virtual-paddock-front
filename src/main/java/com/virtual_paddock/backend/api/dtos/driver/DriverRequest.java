package com.virtual_paddock.backend.api.dtos.driver;

import com.virtual_paddock.backend.utils.enums.DriverStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverRequest {
    @NotBlank(message = "El nombre del piloto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 100, message = "El gamertag no puede exceder 100 caracteres")
    private String gamertag;

    @Size(max = 50, message = "La nacionalidad no puede exceder 50 caracteres")
    private String nationality;

    @Size(max = 10, message = "El dorsal no puede exceder 10 caracteres")
    private String carNumber; // ej: "44", "1", "99"

    @Size(max = 100, message = "El modelo de auto no puede exceder 100 caracteres")
    private String carModel; // ej: "Porsche 992 GT3 R"

    @NotNull(message = "El estado del piloto es obligatorio")
    private DriverStatus status;

    @NotNull(message = "El ID del equipo es obligatorio")
    private Long teamId;
}
