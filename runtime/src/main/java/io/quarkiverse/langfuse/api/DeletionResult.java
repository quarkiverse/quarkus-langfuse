package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The outcome of a delete operation, holding one {@link DeletionOutcome} per distinct identifier.
 *
 * <p>
 * Returned by every {@code deleteById}, {@code deleteByName} and {@code deleteByProvider} operation,
 * whether one identifier was passed or many. Delete operations never fail fast: a batch attempts
 * every identifier and accumulates what happened to each, so a single failure neither hides the
 * successes nor prevents the remaining work.
 *
 * <pre>{@code
 * var result = langfuse.models().deleteByName("gpt-4o", "gpt-4o-mini", "retired-model");
 *
 * if (result.hasFailures()) {
 *     result.failed().forEach((identifier, cause) -> ...);
 * }
 * }</pre>
 *
 * <p>
 * <strong>Keyed by identifier, not ordered.</strong> Deletes may run concurrently, so completion
 * order carries no meaning and is deliberately not part of this contract. Look outcomes up by the
 * identifier you supplied rather than by position. The backing map does happen to iterate in
 * first-occurrence order of the input, which is convenient in tests and logs, but that is an
 * implementation detail and must not be relied upon.
 *
 * <p>
 * Identifiers are deduplicated before any request is made, so this holds one outcome per
 * <em>distinct</em> identifier and {@link #size()} may be smaller than the number of identifiers
 * passed.
 *
 * @see DeletionOutcome
 */
public sealed interface DeletionResult permits DefaultDeletionResult {

    /**
     * Creates a result from the given outcomes.
     *
     * <p>
     * Where two outcomes share an identifier the first one wins, which cannot arise from this layer's
     * own operations since they deduplicate their input up front.
     *
     * @param outcomes the outcomes to collect, must not be {@code null}, and must not contain
     *        {@code null} elements; may be empty
     * @return the result
     * @throws IllegalArgumentException if {@code outcomes} is {@code null} or contains a {@code null}
     *         element
     */
    static DeletionResult of(Collection<DeletionOutcome> outcomes) {
        return new DefaultDeletionResult(outcomes);
    }

    /**
     * The outcome recorded for the given identifier.
     *
     * @param identifier the identifier to look up, may be {@code null}
     * @return the outcome, or empty if this result holds none for that identifier
     */
    Optional<DeletionOutcome> outcome(String identifier);

    /**
     * Every outcome, keyed by identifier.
     *
     * @return an unmodifiable map of identifier to outcome, never {@code null}
     */
    Map<String, DeletionOutcome> outcomes();

    /**
     * The identifiers whose resources were deleted.
     *
     * @return an unmodifiable set of identifiers, never {@code null}
     */
    default Set<String> deleted() {
        return identifiersOf(DeletionOutcome.Deleted.class);
    }

    /**
     * The identifiers no resource matched.
     *
     * <p>
     * These are not failures - see {@link DeletionOutcome.NotFound}.
     *
     * @return an unmodifiable set of identifiers, never {@code null}
     */
    default Set<String> notFound() {
        return identifiersOf(DeletionOutcome.NotFound.class);
    }

    /**
     * The identifiers whose deletes could not be completed, with what went wrong for each.
     *
     * @return an unmodifiable map of identifier to cause, never {@code null}
     */
    default Map<String, Throwable> failed() {
        return outcomes().values()
                .stream()
                .filter(DeletionOutcome.Failed.class::isInstance)
                .map(DeletionOutcome.Failed.class::cast)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                DeletionOutcome::identifier,
                                DeletionOutcome.Failed::cause,
                                (first, second) -> first,
                                LinkedHashMap::new),
                        Collections::unmodifiableMap));
    }

    /**
     * Whether any delete could not be completed.
     *
     * <p>
     * Identifiers that matched nothing do not count: absence is an expected outcome, not a failure.
     *
     * @return {@code true} if at least one outcome is a {@link DeletionOutcome.Failed}
     */
    default boolean hasFailures() {
        return outcomes().values()
                .stream()
                .anyMatch(DeletionOutcome.Failed.class::isInstance);
    }

    /**
     * How many distinct identifiers this result describes.
     *
     * @return the number of outcomes
     */
    default int size() {
        return outcomes().size();
    }

    private Set<String> identifiersOf(Class<? extends DeletionOutcome> branch) {
        return outcomes().values()
                .stream()
                .filter(branch::isInstance)
                .map(DeletionOutcome::identifier)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        Collections::unmodifiableSet));
    }
}
