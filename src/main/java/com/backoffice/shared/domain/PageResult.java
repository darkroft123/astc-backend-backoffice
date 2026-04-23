package com.backoffice.shared.domain;

import java.util.List;

public record PageResult<T>(List<T> items, long totalCount, int page, int size) {

    public int totalPages() {
        return size > 0 ? (int) Math.ceil((double) totalCount / size) : 0;
    }

    public boolean hasNextPage() {
        return page < totalPages() - 1;
    }
}
