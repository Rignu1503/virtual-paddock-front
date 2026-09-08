package com.virtual_paddock.backend.infrastructure.helper;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import org.springframework.data.domain.Page;

import java.util.function.Function;

public final class PageResponseHelper {

    private PageResponseHelper() {
        // Utility class
    }

    public static <E, D> PageResponse<D> fromPage(Page<E> page, Function<E, D> mapper) {
        return PageResponse.<D>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
