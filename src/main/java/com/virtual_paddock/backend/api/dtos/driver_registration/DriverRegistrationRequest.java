package com.virtual_paddock.backend.api.dtos.driver_registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRegistrationRequest {

    @NotBlank(message = "El nombre del piloto es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 100, message = "El gamertag no puede exceder 100 caracteres")
    private String gamertag;

    @Size(max = 50, message = "La nacionalidad no puede exceder 50 caracteres")
    private String nationality;

    @Size(max = 10, message = "El número no puede exceder 10 caracteres")
    private String carNumber;

    @Size(max = 100, message = "El modelo del auto no puede exceder 100 caracteres")
    private String carModel;

    @Email(message = "El correo electrónico debe ser válido")
    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    private String email;

    @Size(max = 100, message = "El tag de Discord no puede exceder 100 caracteres")
    private String discordTag;

    @Size(max = 20, message = "El tipo de contacto no puede exceder 20 caracteres")
    private String contactType;

    @Size(max = 50, message = "El número de WhatsApp no puede exceder 50 caracteres")
    private String phoneWhatsapp;

    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    @NotNull(message = "El ID de la liga es requerido")
    private UUID leagueId;

    private UUID championshipId;

    private UUID preferredTeamId;
}
