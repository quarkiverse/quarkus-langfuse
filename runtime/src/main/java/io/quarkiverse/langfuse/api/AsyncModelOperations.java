package io.quarkiverse.langfuse.api;

import java.util.Objects;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;

import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse model definitions, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#models()}. The asynchronous counterpart of
 * {@link ModelOperations}; the two behave identically apart from how absence is represented, which
 * follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * <pre>{@code
 * langfuse.async().models().findByName("gpt-4o")
 *         .onItem().ifNull().failWith(() -> new IllegalStateException("gpt-4o is not registered"))
 *         .map(Model::getId);
 * }</pre>
 *
 * @see ModelOperations
 */
public sealed interface AsyncModelOperations extends AsyncPagedOperations<Model> permits DefaultAsyncModelOperations {

    /**
     * Finds a model definition by its exact name.
     *
     * <p>
     * <strong>Emits {@code null} if no model has that name.</strong>
     *
     * <p>
     * Names are matched exactly, as Langfuse stores them. Every {@code findByName} operation in this
     * layer matches the same way, whether the match is performed by the server or while walking the
     * collection here.
     *
     * <p>
     * Langfuse offers no name filter for models, so this walks the collection and stops at the first
     * match: a model found in the first page costs a single request.
     *
     * @param modelName the model name to look for, must not be {@code null} or blank
     * @return the matching model, or {@code null} if no model has that name
     * @throws IllegalArgumentException if {@code modelName} is {@code null} or blank
     */
    Uni<Model> findByName(String modelName);

    /**
     * Whether a model definition with the given exact name exists.
     *
     * @param modelName the model name to look for, must not be {@code null} or blank
     * @return {@code true} if a model with that name exists. Never {@code null}
     * @throws IllegalArgumentException if {@code modelName} is {@code null} or blank
     */
    default Uni<Boolean> exists(String modelName) {
        return findByName(modelName)
                .map(Objects::nonNull);
    }

    /**
     * Returns the model definition with the requested name, creating it if no model has that name.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no create-or-update-by-name operation for models, so
     * this performs a lookup followed by a create. Concurrent callers may therefore both observe the
     * model as absent and both create it.
     *
     * @param request the model definition to create if it is missing
     * @return the existing or newly created model definition. Never {@code null}
     */
    Uni<Model> createIfAbsent(CreateModelRequest request);
}
