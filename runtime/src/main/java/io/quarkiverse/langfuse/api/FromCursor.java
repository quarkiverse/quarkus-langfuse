package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.OptionalInt;

record FromCursor(Cursor start) implements CursorSelection {

    FromCursor {
        if (start == null) {
            throw new IllegalArgumentException("Start cursor must not be null");
        }
    }

    @Override
    public Optional<Cursor> startCursor() {
        return Optional.of(start);
    }

    @Override
    public OptionalInt limit() {
        return OptionalInt.of(start.limit());
    }

    @Override
    public OptionalInt batchCount() {
        return OptionalInt.empty();
    }
}
