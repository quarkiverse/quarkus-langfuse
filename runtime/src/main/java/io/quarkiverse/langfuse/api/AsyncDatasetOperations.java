package io.quarkiverse.langfuse.api;

import java.util.Objects;

import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.Dataset;

import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse datasets, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#datasets()}. The asynchronous counterpart of
 * {@link DatasetOperations}; the two behave identically apart from how absence is represented, which
 * follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * @see DatasetOperations
 */
public sealed interface AsyncDatasetOperations extends AsyncPagedOperations<Dataset> permits DefaultAsyncDatasetOperations {

    /**
     * Finds a dataset by its exact name.
     *
     * <p>
     * <strong>Emits {@code null} if no dataset has that name.</strong>
     *
     * @param datasetName the dataset name to look for, must not be {@code null} or blank
     * @return the matching dataset, or {@code null} if no dataset has that name
     * @throws IllegalArgumentException if {@code datasetName} is {@code null} or blank
     */
    Uni<Dataset> findByName(String datasetName);

    /**
     * Whether a dataset with the given exact name exists.
     *
     * @param datasetName the dataset name to look for, must not be {@code null} or blank
     * @return {@code true} if a dataset with that name exists. Never {@code null}
     * @throws IllegalArgumentException if {@code datasetName} is {@code null} or blank
     */
    default Uni<Boolean> exists(String datasetName) {
        return findByName(datasetName)
                .map(Objects::nonNull);
    }

    /**
     * Returns the dataset with the requested name, creating it if no dataset has that name.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no create-or-update-by-name operation for datasets,
     * so this performs a lookup followed by a create. Concurrent callers may therefore both observe the
     * dataset as absent and both create it.
     *
     * @param request the dataset to create if it is missing
     * @return the existing or newly created dataset. Never {@code null}
     */
    Uni<Dataset> createIfAbsent(CreateDatasetRequest request);
}
