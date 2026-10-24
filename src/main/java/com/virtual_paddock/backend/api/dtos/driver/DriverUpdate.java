package com.virtual_paddock.backend.api.dtos.driver;

import com.virtual_paddock.backend.utils.enums.DriverStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

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

    @Size(max = 100, message = "El Steam ID no puede exceder 100 caracteres")
    private String steamId;

    private DriverStatus status;
    private UUID teamId;

    @Size(max = 2048, message = "La URL del avatar no puede exceder 2048 caracteres")
    private String avatarUrl;

    @Size(max = 255, message = "La URL de Twitch no puede exceder 255 caracteres")
    private String twitchUrl;

    @Size(max = 255, message = "La URL de Kick no puede exceder 255 caracteres")
    private String kickUrl;

    @Size(max = 255, message = "La URL de TikTok no puede exceder 255 caracteres")
    private String tiktokUrl;

    @Size(max = 255, message = "La URL de YouTube no puede exceder 255 caracteres")
    private String youtubeUrl;

    @Size(max = 500, message = "La biografía no puede exceder 500 caracteres")
    private String bio;
}
