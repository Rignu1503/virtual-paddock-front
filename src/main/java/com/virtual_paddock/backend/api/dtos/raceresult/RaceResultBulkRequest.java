package com.virtual_paddock.backend.api.dtos.raceresult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceResultBulkRequest {

    @NotEmpty(message = "La lista de resultados no puede estar vacía")
    @Valid
    private List<RaceResultBulkItemRequest> results;
}
