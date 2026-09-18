package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.DatasetItem;

import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;

/**
 * Higher-level operations over Langfuse dataset items.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#datasetItems()}. Dataset items are a page-addressed
 * collection, so {@link #stream}, {@link #streamPages} and {@link #findPage} - inherited from
 * {@link PagedOperations} - accept a {@link PageSelection} or a {@link Page} directly.
 *
 * <p>
 * <strong>This is not {@link DatasetOperations}.</strong> That domain covers the datasets
 * themselves and is keyed by dataset name; this one covers the items inside them, which are a
 * top-level collection Langfuse addresses by item id and narrows with a
 * {@link DatasetItemFilter#datasetName() datasetName} filter.
 *
 * <p>
 * <strong>Dataset items have no name.</strong> An item is identified by its id alone, so this domain
 * offers {@link #findById(String)} and no name-based lookup, and there is no {@code createIfAbsent}:
 * there is no key on which "already there" could be decided.
 *
 * <pre>{@code
 * langfuse.datasetItems().findById("item-1");
 * langfuse.datasetItems().matching(filter).findAll();
 * langfuse.datasetItems().deleteById("item-1");
 * }</pre>
 *
 * @see AsyncDatasetItemOperations
 */
public sealed interface DatasetItemOperations extends PagedOperations<DatasetItem> permits DefaultDatasetItemOperations {

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
     * @return a view of this collection restricted to the matching dataset items
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    DatasetItemOperations matching(DatasetItemFilter filter);

    /**
     * Finds a dataset item by its id.
     *
     * <p>
     * This is a <strong>direct lookup</strong>: Langfuse resolves the id server-side, so it costs a
     * single request whatever the size of the collection.
     *
     * <p>
     * The lookup is not scoped by the view's filter - an item is fetched by id alone, so calling this
     * on a filtered view returns the same item as calling it on the unfiltered collection.
     *
     * @param id the dataset item id to look for, must not be {@code null} or blank
     * @return the matching dataset item, or empty if no item has that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         item not existing
     */
    Optional<DatasetItem> findById(String id);

    /**
     * Creates a dataset item.
     *
     * <p>
     * The dataset the item belongs to is named on the request itself, and Langfuse answers with the
     * stored item.
     *
     * @param request the dataset item to create, must not be {@code null}
     * @return the created dataset item
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws com.langfuse.api.LangfuseApiException if the request fails
     */
    DatasetItem create(CreateDatasetItemRequest request);

    /**
     * Deletes the dataset items with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no dataset item yields a
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
     * <strong>No collection scan.</strong> The Langfuse delete endpoint is keyed on the item id
     * itself, so each id is deleted in a single request and an absent id costs no more than a present
     * one.
     *
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncDatasetItemOperations#deleteById(Collection)} instead.
     *
     * @param ids the dataset item ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteById(Collection<String> ids);

    /**
     * Deletes the dataset item with the given id.
     *
     * @param id the dataset item id to delete, must not be {@code null} or blank
     * @return the outcome for that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the dataset items with the given ids.
     *
     * @param ids the dataset item ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }
}
