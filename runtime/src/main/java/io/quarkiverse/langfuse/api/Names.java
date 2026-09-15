package io.quarkiverse.langfuse.api;

/**
 * Validates the natural-key arguments the lookup operations are given.
 */
final class Names {

    private Names() {
    }

    static String require(String value, String description) {
        if ((value == null) || value.isBlank()) {
            throw new IllegalArgumentException("%s must not be null or blank".formatted(description));
        }

        return value;
    }
}
