package com.virtual_paddock.backend.api.dtos.league;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueUpdate {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 100, message = "El slug no puede exceder 100 caracteres")
    private String slugUrl;

    @Size(max = 255, message = "El tagline no puede exceder 255 caracteres")
    private String tagline;

    @Size(max = 7, message = "El color debe ser un código hexadecimal válido")
    private String accentColor;

    private String surfaceTheme;

    private String discordUrl;
    private String twitchUrl;
    private String kickUrl;
    private String tiktokUrl;
    private String youtubeUrl;
    private String registrationUrl;
    private String rulesUrl;
}
