package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

    /**
     * Deletes the model definitions with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no model yields a
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
     * @param ids the model ids to delete, must not be {@code null} and must not contain {@code null}
     *        or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteById(Collection<String> ids);

    /**
     * Deletes the model definition with the given id.
     *
     * @param id the model id to delete, must not be {@code null} or blank
     * @return the outcome for that id. Never {@code null}
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the model definitions with the given ids.
     *
     * @param ids the model ids to delete, must not be {@code null} and must not contain {@code null}
     *        or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }

    /**
     * Deletes the model definitions with the given exact names.
     *
     * <p>
     * <strong>Absence is not an error.</strong> A name matching no model yields a
     * {@link DeletionOutcome.NotFound} outcome rather than failing the {@link Uni}, so deleting
     * something that is already gone is a normal result rather than a failure to handle.
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
     * @param modelNames the model names to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name, keyed by the name supplied rather than by the resolved
     *         id. Never {@code null}
     * @throws IllegalArgumentException if {@code modelNames} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteByName(Collection<String> modelNames);

    /**
     * Deletes the model definition with the given exact name.
     *
     * @param modelName the model name to delete, must not be {@code null} or blank
     * @return the outcome for that name. Never {@code null}
     * @throws IllegalArgumentException if {@code modelName} is {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default Uni<DeletionResult> deleteByName(String modelName) {
        return deleteByName(Collections.singletonList(modelName));
    }

    /**
     * Deletes the model definitions with the given exact names.
     *
     * @param modelNames the model names to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name. Never {@code null}
     * @throws IllegalArgumentException if {@code modelNames} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default Uni<DeletionResult> deleteByName(String... modelNames) {
        return deleteByName((modelNames == null) ? null : Arrays.asList(modelNames));
    }
}
