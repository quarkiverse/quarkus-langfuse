package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.Optional;

record DefaultPagedResult<T>(List<T> items, Page page, int totalItems, int totalPages) implements PagedResult<T> {

    DefaultPagedResult {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null");
        }

        items = (items == null) ? List.of() : List.copyOf(items);
        totalItems = Math.max(0, totalItems);
        totalPages = Math.max(0, totalPages);
    }

    @Override
    public Optional<Page> nextPage() {
        // There's a next page exactly when this one wasn't the last: index 3 of 3 has none, index 2 of 3
        // does. Deriving it from totalPages rather than trusting a separate hasNext flag means it's
        // always consistent with the totals this same result reports.
        return Optional.of(page)
                .filter(current -> current.index() < totalPages)
                .map(Page::next);
    }
}
