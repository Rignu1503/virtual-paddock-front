package com.virtual_paddock.backend.api.dtos.league;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueBasicResponse {

    private Long id;
    private String name;
}
