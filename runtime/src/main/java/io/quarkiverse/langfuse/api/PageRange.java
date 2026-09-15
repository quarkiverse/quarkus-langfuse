package io.quarkiverse.langfuse.api;

import java.util.OptionalInt;

record PageRange(Page start, int count) implements PageSelection {

    PageRange {
        if (start == null) {
            throw new IllegalArgumentException("Start page must not be null");
        }

        if (count < 0) {
            throw new IllegalArgumentException("Page count must not be negative, but was %d".formatted(count));
        }
    }

    @Override
    public int startIndex() {
        return start.index();
    }

    @Override
    public OptionalInt pageSize() {
        return OptionalInt.of(start.size());
    }

    @Override
    public OptionalInt pageCount() {
        return OptionalInt.of(count);
    }
}
