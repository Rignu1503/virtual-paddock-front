package com.virtual_paddock.backend.api.dtos.championship;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionshipBasicResponse {

    private Long id;
    private String gameName;
}
