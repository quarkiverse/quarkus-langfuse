package io.quarkiverse.langfuse.api;

import java.util.OptionalInt;

record SinglePage(Page page) implements PageSelection {

    SinglePage {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null");
        }
    }

    @Override
    public int startIndex() {
        return page.index();
    }

    @Override
    public OptionalInt pageSize() {
        return OptionalInt.of(page.size());
    }

    @Override
    public OptionalInt pageCount() {
        return OptionalInt.of(1);
    }
}
