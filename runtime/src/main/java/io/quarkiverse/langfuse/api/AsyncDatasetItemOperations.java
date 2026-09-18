package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.DatasetItem;

import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse dataset items, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#datasetItems()}. The asynchronous counterpart of
 * {@link DatasetItemOperations}; the two behave identically apart from how absence is represented,
 * which follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * <p>
 * <strong>Dataset items have no name</strong>, so this domain offers a lookup by id and no
 * name-based lookup, and no {@code createIfAbsent}.
 *
 * @see DatasetItemOperations
 */
public sealed interface AsyncDatasetItemOperations extends AsyncPagedOperations<DatasetItem>
        permits DefaultAsyncDatasetItemOperations {

    /**
     * A view of this collection restricted to the dataset items matching {@code filter}.
     *
     * <p>
     * <strong>Replaces any filter already applied rather than combining with it.</strong>
     * {@code datasetItems().matching(a).matching(b)} is filtered by {@code b} alone. On the returned
     * view, every inherited operation is scoped to the view: {@link #findAll()} means "every dataset
     * item <em>of this view</em>", not every dataset item in the project.
     *
     * <p>
     * The view is a new instance; this one is unaffected and stays usable.
     *
     * @param filter the criteria to restrict the collection to, must not be {@code null}; use
     *        {@link DatasetItemFilter#none()} for an unrestricted view
     * @return a view of this collection restricted to the matching dataset items. Never {@code null}
     * @throws IllegalArgumentException if {@code filter} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    AsyncDatasetItemOperations matching(DatasetItemFilter filter);

    /**
     * Finds a dataset item by its id.
     *
     * <p>
     * <strong>Emits {@code null} if no dataset item has that id.</strong>
     *
     * <p>
     * This is a <strong>direct lookup</strong>: Langfuse resolves the id server-side, so it costs a
     * single request whatever the size of the collection.
     *
     * <p>
     * The lookup is not scoped by the view's filter - an item is fetched by id alone, so calling this
     * on a filtered view emits the same item as calling it on the unfiltered collection.
     *
     * @param id the dataset item id to look for, must not be {@code null} or blank
     * @return the matching dataset item, or {@code null} if no item has that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank. Thrown from this call
     *         rather than emitted as a failure
     */
    Uni<DatasetItem> findById(String id);

    /**
     * Creates a dataset item.
     *
     * <p>
     * The dataset the item belongs to is named on the request itself, and Langfuse answers with the
     * stored item.
     *
     * @param request the dataset item to create, must not be {@code null}
     * @return the created dataset item. Never {@code null}
     * @throws IllegalArgumentException if {@code request} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    Uni<DatasetItem> create(CreateDatasetItemRequest request);

    /**
     * Deletes the dataset items with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no dataset item yields a
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
     * <strong>No collection scan.</strong> The Langfuse delete endpoint is keyed on the item id
     * itself, so each id is deleted in a single request and an absent id costs no more than a present
     * one.
     *
     * @param ids the dataset item ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteById(Collection<String> ids);

    /**
     * Deletes the dataset item with the given id.
     *
     * @param id the dataset item id to delete, must not be {@code null} or blank
     * @return the outcome for that id. Never {@code null}
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the dataset items with the given ids.
     *
     * @param ids the dataset item ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }
}
