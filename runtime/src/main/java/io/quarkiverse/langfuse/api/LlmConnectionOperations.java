package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

/**
 * Higher-level operations over Langfuse LLM connections.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#llmConnections()}. LLM connections are a page-addressed
 * collection, so {@link #stream}, {@link #streamPages} and {@link #findPage} - inherited from
 * {@link PagedOperations} - accept a {@link PageSelection} or a {@link Page} directly.
 *
 * <p>
 * LLM connections are keyed by {@code provider} rather than by name, and Langfuse offers a genuine
 * create-or-update operation for them, so {@link #upsert(UpsertLlmConnectionRequest)} - unlike the
 * {@code createIfAbsent} operations on the other domains - is atomic.
 *
 * @see AsyncLlmConnectionOperations
 */
public sealed interface LlmConnectionOperations extends PagedOperations<LlmConnection> permits DefaultLlmConnectionOperations {

    /**
     * Finds the LLM connection configured for the given exact provider name.
     *
     * <p>
     * Langfuse offers no provider filter for LLM connections, so this walks the collection and stops at
     * the first match: a connection found in the first page costs a single request.
     *
     * @param provider the provider name to look for, must not be {@code null} or blank
     * @return the matching connection, or empty if no connection is configured for that provider
     * @throws IllegalArgumentException if {@code provider} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         connection not existing
     */
    Optional<LlmConnection> findByProvider(String provider);

    /**
     * Whether an LLM connection is configured for the given exact provider name.
     *
     * @param provider the provider name to look for, must not be {@code null} or blank
     * @return {@code true} if a connection is configured for that provider
     * @throws IllegalArgumentException if {@code provider} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         connection not existing
     */
    default boolean exists(String provider) {
        return findByProvider(provider).isPresent();
    }

    /**
     * Creates the connection for its provider if none exists, or replaces it if one does.
     *
     * <p>
     * <strong>Atomic.</strong> Backed directly by Langfuse's own create-or-update operation, so - unlike
     * the {@code createIfAbsent} operations on the other domains - this is safe under concurrent callers.
     *
     * @param request the connection to create or replace
     * @return the resulting connection
     */
    LlmConnection upsert(UpsertLlmConnectionRequest request);

    /**
     * Deletes the LLM connections with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no LLM connection yields a
     * {@link DeletionOutcome.NotFound} outcome rather than throwing, so deleting something that is
     * already gone is a normal result rather than a failure to handle.
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
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncLlmConnectionOperations#deleteById(Collection)} instead.
     *
     * @param ids the LLM connection ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteById(Collection<String> ids);

    /**
     * Deletes the LLM connection with the given id.
     *
     * @param id the LLM connection id to delete, must not be {@code null} or blank
     * @return the outcome for that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the LLM connections with the given ids.
     *
     * @param ids the LLM connection ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String... ids) {
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
     * {@link DeletionOutcome.NotFound} outcome rather than throwing, so deleting something that is
     * already gone is a normal result rather than a failure to handle.
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
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncLlmConnectionOperations#deleteByProvider(Collection)} instead.
     *
     * @param providers the provider names to delete connections for, must not be {@code null} and
     *        must not contain {@code null} or blank elements; may be empty, in which case no request
     *        is issued
     * @return one outcome per distinct provider, keyed by the provider supplied rather than by the
     *         resolved id
     * @throws IllegalArgumentException if {@code providers} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteByProvider(Collection<String> providers);

    /**
     * Deletes the LLM connection configured for the given exact provider name.
     *
     * @param provider the provider name to delete the connection for, must not be {@code null} or
     *        blank
     * @return the outcome for that provider
     * @throws IllegalArgumentException if {@code provider} is {@code null} or blank
     * @see #deleteByProvider(Collection)
     */
    default DeletionResult deleteByProvider(String provider) {
        return deleteByProvider(Collections.singletonList(provider));
    }

    /**
     * Deletes the LLM connections configured for the given exact provider names.
     *
     * @param providers the provider names to delete connections for, must not be {@code null} and
     *        must not contain {@code null} or blank elements; may be empty, in which case no request
     *        is issued
     * @return one outcome per distinct provider
     * @throws IllegalArgumentException if {@code providers} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByProvider(Collection)
     */
    default DeletionResult deleteByProvider(String... providers) {
        return deleteByProvider((providers == null) ? null : Arrays.asList(providers));
    }
}
