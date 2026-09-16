package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.List;

import io.quarkiverse.langfuse.util.ValidationUtils;

/**
 * Prepares the identifier set a batch delete will act on: validate everything, then deduplicate.
 *
 * <p>
 * Pure and free of I/O, which is why both {@link Deletions} and {@link AsyncDeletions} share it. The
 * independence of the two trees forbids one delegating its <em>execution</em> to the other; it does
 * not forbid a shared pure helper, exactly as {@link PagedResults} and {@link CursorResults} are
 * already shared by both.
 */
final class DeletionIdentifiers {

    private DeletionIdentifiers() {
    }

    /**
     * Validates every identifier, then returns the distinct ones in first-occurrence order.
     *
     * @param identifiers the caller-supplied identifiers
     * @param label what to call an identifier in an error message, e.g. {@code "Model name"}
     * @return the distinct identifiers, in first-occurrence order
     * @throws IllegalArgumentException if {@code identifiers} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    static List<String> validated(Collection<String> identifiers, String label) {
        ValidationUtils.ensureNotNull(identifiers, label);

        // Validates before deduplicating, so a blank identifier is rejected even when it appears more
        // than once. distinct() then keeps the first occurrence of each, which fixes both the set of
        // identifiers that will be deleted and the order they are attempted in.
        return identifiers.stream()
                .map(identifier -> ValidationUtils.ensureNotBlank(identifier, label))
                .distinct()
                .toList();
    }
}
