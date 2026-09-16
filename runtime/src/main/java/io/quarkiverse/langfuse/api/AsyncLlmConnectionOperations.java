package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse LLM connections, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#llmConnections()}. The asynchronous counterpart of
 * {@link LlmConnectionOperations}; the two behave identically apart from how absence is represented,
 * which follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * @see LlmConnectionOperations
 */
public sealed interface AsyncLlmConnectionOperations extends AsyncPagedOperations<LlmConnection>
        permits DefaultAsyncLlmConnectionOperations {

    /**
     * Finds the LLM connection configured for the given exact provider name.
     *
     * <p>
     * <strong>Emits {@code null} if no connection is configured for that provider.</strong>
     *
     * @param provider the provider name to look for, must not be {@code null} or blank
     * @return the matching connection, or {@code null} if none is configured for that provider
     * @throws IllegalArgumentException if {@code provider} is {@code null} or blank
     */
    Uni<LlmConnection> findByProvider(String provider);

    /**
     * Whether an LLM connection is configured for the given exact provider name.
     *
     * @param provider the provider name to look for, must not be {@code null} or blank
     * @return {@code true} if a connection is configured for that provider. Never {@code null}
     * @throws IllegalArgumentException if {@code provider} is {@code null} or blank
     */
    default Uni<Boolean> exists(String provider) {
        return findByProvider(provider)
                .map(Objects::nonNull);
    }

    /**
     * Creates the connection for its provider if none exists, or replaces it if one does.
     *
     * <p>
     * <strong>Atomic.</strong> Backed directly by Langfuse's own create-or-update operation.
     *
     * @param request the connection to create or replace
     * @return the resulting connection. Never {@code null}
     */
    Uni<LlmConnection> upsert(UpsertLlmConnectionRequest request);

    /**
     * Deletes the LLM connections with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no LLM connection yields a
     * {@link DeletionOutcome.NotFound} outcome rather than failing the {@link Uni}, so deleting
     * something that is already gone is a normal result rather than a failure to handle.
     *
     * <p>
     * <strong>Never fails fast.</strong> Every id is attempted regardless of what happened to the
     * others, and each is reported separately: one failure neither hides the successes nor prevents
     * the remaining work.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no bulk delete, so this iterates client-side and
     * can partially apply. Inspect the returned {@link DeletionResult} rather than assuming
     * all-or-nothing.
     *
     * <p>
     * <strong>Evaluators that depend on a deleted connection are automatically paused</strong> by
     * Langfuse. That happens server-side and is not reflected in the returned {@link DeletionResult},
     * so a bulk delete here can pause evaluators this call says nothing about.
     *
     * @param ids the LLM connection ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteById(Collection<String> ids);

    /**
     * Deletes the LLM connection with the given id.
     *
     * @param id the LLM connection id to delete, must not be {@code null} or blank
     * @return the outcome for that id. Never {@code null}
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the LLM connections with the given ids.
     *
     * @param ids the LLM connection ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }

    /**
     * Deletes the LLM connections configured for the given exact provider names.
     *
     * <p>
     * Keyed by provider rather than by name, matching {@link #findByProvider} and {@link #upsert}.
     *
     * <p>
     * <strong>Absence is not an error.</strong> A provider matching no LLM connection yields a
     * {@link DeletionOutcome.NotFound} outcome rather than failing the {@link Uni}, so deleting
     * something that is already gone is a normal result rather than a failure to handle.
     *
     * <p>
     * <strong>Never fails fast.</strong> Every provider is attempted regardless of what happened to
     * the others, and each is reported separately: one failure neither hides the successes nor
     * prevents the remaining work.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no bulk delete, so this iterates client-side and
     * can partially apply. Inspect the returned {@link DeletionResult} rather than assuming
     * all-or-nothing.
     *
     * <p>
     * <strong>Evaluators that depend on a deleted connection are automatically paused</strong> by
     * Langfuse. That happens server-side and is not reflected in the returned {@link DeletionResult},
     * so a bulk delete here can pause evaluators this call says nothing about.
     *
     * <p>
     * Each provider is resolved to its id first, which walks the collection the way
     * {@link #findByProvider} does. A provider that matches stops the scan early, but an
     * <strong>absent</strong> provider costs a full traversal, so deleting many absent providers is
     * markedly more expensive than deleting the same number of ids. Prefer
     * {@link #deleteById(Collection)} where ids are already known.
     *
     * @param providers the provider names to delete connections for, must not be {@code null} and
     *        must not contain {@code null} or blank elements; may be empty, in which case no request
     *        is issued
     * @return one outcome per distinct provider, keyed by the provider supplied rather than by the
     *         resolved id. Never {@code null}
     * @throws IllegalArgumentException if {@code providers} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteByProvider(Collection<String> providers);

    /**
     * Deletes the LLM connection configured for the given exact provider name.
     *
     * @param provider the provider name to delete the connection for, must not be {@code null} or
     *        blank
     * @return the outcome for that provider. Never {@code null}
     * @throws IllegalArgumentException if {@code provider} is {@code null} or blank
     * @see #deleteByProvider(Collection)
     */
    default Uni<DeletionResult> deleteByProvider(String provider) {
        return deleteByProvider(Collections.singletonList(provider));
    }

    /**
     * Deletes the LLM connections configured for the given exact provider names.
     *
     * @param providers the provider names to delete connections for, must not be {@code null} and
     *        must not contain {@code null} or blank elements; may be empty, in which case no request
     *        is issued
     * @return one outcome per distinct provider. Never {@code null}
     * @throws IllegalArgumentException if {@code providers} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByProvider(Collection)
     */
    default Uni<DeletionResult> deleteByProvider(String... providers) {
        return deleteByProvider((providers == null) ? null : Arrays.asList(providers));
    }
}
