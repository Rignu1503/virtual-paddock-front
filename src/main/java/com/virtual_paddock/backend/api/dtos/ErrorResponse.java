package com.virtual_paddock.backend.api.dtos;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO estándar para errores de validación y errores generales.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private int status;
    private String message;
    private List<Map<String, String>> errors;
}
