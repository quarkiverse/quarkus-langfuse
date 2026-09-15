package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.Dataset;

/**
 * Higher-level operations over Langfuse datasets.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#datasets()}. Datasets are a page-addressed collection, so
 * {@link #stream}, {@link #streamPages} and {@link #findPage} - inherited from
 * {@link PagedOperations} - accept a {@link PageSelection} or a {@link Page} directly.
 *
 * <p>
 * Unlike the other domains in this layer, Langfuse can look datasets up by name directly
 * ({@code GET /api/public/v2/datasets/{name}}), so {@link #findByName(String)} and
 * {@link #exists(String)} cost a single request rather than a paginated scan.
 *
 * @see AsyncDatasetOperations
 */
public sealed interface DatasetOperations extends PagedOperations<Dataset> permits DefaultDatasetOperations {

    /**
     * Finds a dataset by its exact name.
     *
     * @param datasetName the dataset name to look for, must not be {@code null} or blank
     * @return the matching dataset, or empty if no dataset has that name
     * @throws IllegalArgumentException if {@code datasetName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         dataset not existing
     */
    Optional<Dataset> findByName(String datasetName);

    /**
     * Whether a dataset with the given exact name exists.
     *
     * @param datasetName the dataset name to look for, must not be {@code null} or blank
     * @return {@code true} if a dataset with that name exists
     * @throws IllegalArgumentException if {@code datasetName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         dataset not existing
     */
    default boolean exists(String datasetName) {
        return findByName(datasetName).isPresent();
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
     * @return the existing or newly created dataset
     */
    Dataset createIfAbsent(CreateDatasetRequest request);
}
