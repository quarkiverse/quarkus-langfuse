package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.OptionalInt;

record SingleCursor(Cursor cursor) implements CursorSelection {

    SingleCursor {
        if (cursor == null) {
            throw new IllegalArgumentException("Cursor must not be null");
        }
    }

    @Override
    public Optional<Cursor> startCursor() {
        return Optional.of(cursor);
    }

    @Override
    public OptionalInt limit() {
        return OptionalInt.of(cursor.limit());
    }

    @Override
    public OptionalInt batchCount() {
        return OptionalInt.of(1);
    }
}
