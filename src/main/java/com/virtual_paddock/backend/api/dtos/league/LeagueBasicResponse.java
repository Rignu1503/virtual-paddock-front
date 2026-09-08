package com.virtual_paddock.backend.api.dtos.league;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueBasicResponse {

    private UUID id;
    private String name;
    private String slugUrl;
    private String tagline;
    private String accentColor;
    private String surfaceTheme;
}
