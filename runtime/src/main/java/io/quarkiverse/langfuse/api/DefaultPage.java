package io.quarkiverse.langfuse.api;

record DefaultPage(int index, int size) implements Page {

    DefaultPage {
        if (index < 1) {
            throw new IllegalArgumentException(
                    "Page index must be greater than or equal to 1 (page indexes are 1-based), but was %d".formatted(index));
        }

        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than or equal to 1, but was %d".formatted(size));
        }
    }

    @Override
    public Page next() {
        return new DefaultPage(index + 1, size);
    }
}
