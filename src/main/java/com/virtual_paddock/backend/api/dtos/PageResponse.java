package com.virtual_paddock.backend.api.dtos;

import lombok.*;

import java.util.List;

/**
 * DTO genérico de paginación para envolver resultados paginados.
 * Se usa como ApiResponse<PageResponse<T>> en las respuestas de listado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
