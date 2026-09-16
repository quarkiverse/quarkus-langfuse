package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

    /**
     * Deletes the model definitions with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no model yields a
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
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncModelOperations#deleteById(Collection)} instead.
     *
     * @param ids the model ids to delete, must not be {@code null} and must not contain {@code null}
     *        or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteById(Collection<String> ids);

    /**
     * Deletes the model definition with the given id.
     *
     * @param id the model id to delete, must not be {@code null} or blank
     * @return the outcome for that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the model definitions with the given ids.
     *
     * @param ids the model ids to delete, must not be {@code null} and must not contain {@code null}
     *        or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }

    /**
     * Deletes the model definitions with the given exact names.
     *
     * <p>
     * <strong>Absence is not an error.</strong> A name matching no model yields a
     * {@link DeletionOutcome.NotFound} outcome rather than throwing, so deleting something that is
     * already gone is a normal result rather than a failure to handle.
     *
     * <p>
     * <strong>Never fails fast.</strong> Every name is attempted regardless of what happened to the
     * others, and each is reported separately: one failure neither hides the successes nor prevents
     * the remaining work.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no bulk delete, so this iterates client-side and
     * can partially apply. Inspect the returned {@link DeletionResult} rather than assuming
     * all-or-nothing.
     *
     * <p>
     * Each name is resolved to its id first, which walks the collection the way {@link #findByName}
     * does. A name that matches stops the scan early, but an <strong>absent</strong> name costs a
     * full traversal, so deleting many absent names is markedly more expensive than deleting the
     * same number of ids. Prefer {@link #deleteById(Collection)} where ids are already known.
     *
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncModelOperations#deleteByName(Collection)} instead.
     *
     * @param modelNames the model names to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name, keyed by the name supplied rather than by the resolved id
     * @throws IllegalArgumentException if {@code modelNames} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteByName(Collection<String> modelNames);

    /**
     * Deletes the model definition with the given exact name.
     *
     * @param modelName the model name to delete, must not be {@code null} or blank
     * @return the outcome for that name
     * @throws IllegalArgumentException if {@code modelName} is {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default DeletionResult deleteByName(String modelName) {
        return deleteByName(Collections.singletonList(modelName));
    }

    /**
     * Deletes the model definitions with the given exact names.
     *
     * @param modelNames the model names to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name
     * @throws IllegalArgumentException if {@code modelNames} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default DeletionResult deleteByName(String... modelNames) {
        return deleteByName((modelNames == null) ? null : Arrays.asList(modelNames));
    }
}
