package com.rml.system.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Paginated response wrapper aligned with EasyUI datagrid protocol.
 * All paginated list endpoints return this shape.
 *
 * @param <T> row type
 */
@Getter
@Builder
public class PageResponse<T> {

    private final long total;
    private final java.util.List<T> rows;

    public static <T> PageResponse<T> of(long total, java.util.List<T> rows) {
        return PageResponse.<T>builder()
                .total(total)
                .rows(rows)
                .build();
    }
}
