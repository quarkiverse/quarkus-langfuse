package io.quarkiverse.langfuse.api;

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
}
