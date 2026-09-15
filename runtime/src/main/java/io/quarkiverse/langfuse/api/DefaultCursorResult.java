package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.Optional;

record DefaultCursorResult<T>(List<T> items, Cursor cursor, String nextCursorValue) implements CursorResult<T> {

    DefaultCursorResult {
        if (cursor == null) {
            throw new IllegalArgumentException("Cursor must not be null");
        }

        items = (items == null) ? List.of() : List.copyOf(items);
    }

    @Override
    public Optional<Cursor> nextCursor() {
        // A blank cursor is folded in with a null one and treated as "no further data", the same way an
        // absent one is - defensive against a server that sends an empty string rather than omitting the
        // field, since either way there is nowhere further to go.
        return Optional.ofNullable(nextCursorValue)
                .filter(value -> !value.isBlank())
                .map(value -> Cursor.at(value, cursor.limit()));
    }
}
