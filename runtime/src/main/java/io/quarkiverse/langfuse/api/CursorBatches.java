package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.OptionalInt;

record CursorBatches(Cursor start, int count) implements CursorSelection {

    CursorBatches {
        if (start == null) {
            throw new IllegalArgumentException("Start cursor must not be null");
        }

        if (count < 0) {
            throw new IllegalArgumentException("Batch count must not be negative, but was %d".formatted(count));
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
        return OptionalInt.of(count);
    }
}
