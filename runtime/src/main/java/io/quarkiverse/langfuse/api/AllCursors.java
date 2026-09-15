package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.OptionalInt;

record AllCursors(OptionalInt limit) implements CursorSelection {

    @Override
    public Optional<Cursor> startCursor() {
        return Optional.empty();
    }

    @Override
    public OptionalInt batchCount() {
        return OptionalInt.empty();
    }
}
