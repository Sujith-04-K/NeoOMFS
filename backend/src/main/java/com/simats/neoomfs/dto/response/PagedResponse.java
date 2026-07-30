package com.simats.neoomfs.dto.response;

import lombok.*;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
