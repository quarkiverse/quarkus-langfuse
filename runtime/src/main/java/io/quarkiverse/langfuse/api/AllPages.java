package io.quarkiverse.langfuse.api;

import java.util.OptionalInt;

record AllPages(OptionalInt pageSize) implements PageSelection {

    @Override
    public int startIndex() {
        return 1;
    }

    @Override
    public OptionalInt pageCount() {
        return OptionalInt.empty();
    }
}
