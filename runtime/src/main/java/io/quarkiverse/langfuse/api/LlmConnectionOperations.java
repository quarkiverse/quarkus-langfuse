package io.quarkiverse.langfuse.api;

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
}
