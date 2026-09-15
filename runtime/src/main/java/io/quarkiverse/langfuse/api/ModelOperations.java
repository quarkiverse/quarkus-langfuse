package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;

/**
 * Higher-level operations over Langfuse model definitions.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#models()}. Models are a page-addressed collection, so
 * {@link #stream}, {@link #streamPages} and {@link #findPage} - inherited from
 * {@link PagedOperations} - accept a {@link PageSelection} or a {@link Page} directly.
 *
 * <pre>{@code
 * langfuse.models().findByName("gpt-4o");
 * langfuse.models().find(PageSelection.rangeClosed(2, 3, 75));
 * }</pre>
 *
 * @see AsyncModelOperations
 */
public sealed interface ModelOperations extends PagedOperations<Model> permits DefaultModelOperations {

    /**
     * Finds a model definition by its exact name.
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
     * @return the matching model, or empty if no model has that name
     * @throws IllegalArgumentException if {@code modelName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         model not existing
     */
    Optional<Model> findByName(String modelName);

    /**
     * Whether a model definition with the given exact name exists.
     *
     * @param modelName the model name to look for, must not be {@code null} or blank
     * @return {@code true} if a model with that name exists
     * @throws IllegalArgumentException if {@code modelName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         model not existing
     */
    default boolean exists(String modelName) {
        return findByName(modelName).isPresent();
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
     * @return the existing or newly created model definition
     */
    Model createIfAbsent(CreateModelRequest request);
}
