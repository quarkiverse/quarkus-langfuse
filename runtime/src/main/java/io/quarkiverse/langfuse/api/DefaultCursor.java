package io.quarkiverse.langfuse.api;

import java.util.Optional;

record DefaultCursor(String cursorValue, int limit) implements Cursor {

    DefaultCursor {
        if (limit < 1) {
            throw new IllegalArgumentException("Cursor limit must be greater than or equal to 1, but was %d".formatted(limit));
        }
    }

    @Override
    public Optional<String> value() {
        return Optional.ofNullable(cursorValue);
    }
}
