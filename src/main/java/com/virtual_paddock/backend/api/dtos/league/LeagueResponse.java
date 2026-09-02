package com.virtual_paddock.backend.api.dtos.league;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueResponse {

    private Long id;
    private String name;
    private String slugUrl;
    private String tagline;
    private String accentColor;
    private String surfaceTheme;
    private String discordUrl;
    private String twitchUrl;
    private String kickUrl;
    private String tiktokUrl;
    private String youtubeUrl;
    private String registrationUrl;
    private String rulesUrl;
    private Long userId;
}
