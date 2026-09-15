package io.quarkiverse.langfuse.api;

import java.util.OptionalInt;

record FromPage(Page start) implements PageSelection {

    FromPage {
        if (start == null) {
            throw new IllegalArgumentException("Start page must not be null");
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
        return OptionalInt.empty();
    }
}
