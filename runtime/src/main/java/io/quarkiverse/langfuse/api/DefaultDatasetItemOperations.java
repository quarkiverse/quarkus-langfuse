package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Optional;

import com.langfuse.api.datasetItems.DatasetItemsApi;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsCreateRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsDeleteRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsGetRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsListRequest;
import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.DatasetItem;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultDatasetItemOperations extends AbstractPagedOperations<DatasetItem> implements DatasetItemOperations {
    private final DatasetItemsApi datasetItemsApi;
    private final LangfuseConfig config;

    DefaultDatasetItemOperations(DatasetItemsApi datasetItemsApi, LangfuseConfig config) {
        this(datasetItemsApi, config, DatasetItemFilter.none());
    }

    // The filter is captured in the fetcher lambda handed to the engine, which is why a filtered view
    // needs neither a widened PageFetcher nor a change to Pagination: a view is just this class
    // constructed over a different fetcher.
    private DefaultDatasetItemOperations(DatasetItemsApi datasetItemsApi, LangfuseConfig config,
            DatasetItemFilter filter) {
        super(page -> fetch(datasetItemsApi, page, filter), config);
        this.datasetItemsApi = datasetItemsApi;
        this.config = config;
    }

    @Override
    public DatasetItemOperations matching(DatasetItemFilter filter) {
        return new DefaultDatasetItemOperations(this.datasetItemsApi, this.config,
                ValidationUtils.ensureNotNull(filter, "Filter"));
    }

    @Override
    public Optional<DatasetItem> findById(String id) {
        var itemId = ValidationUtils.ensureNotBlank(id, "Dataset item id");

        // Only LangfuseNotFoundException is caught - catching LangfuseApiException would report a 401 or
        // a 500 as "absent", which is the one mistake this layer must never make.
        try {
            return Optional.of(this.datasetItemsApi.datasetItemsGet(APIDatasetItemsGetRequest.newBuilder()
                    .id(itemId)
                    .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public DatasetItem create(CreateDatasetItemRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        return this.datasetItemsApi.datasetItemsCreate(APIDatasetItemsCreateRequest.newBuilder()
                .createDatasetItemRequest(request)
                .build());
    }

    @Override
    public DeletionResult deleteById(Collection<String> ids) {
        // Optional::of is the identity resolver: the delete endpoint takes the item id directly, so
        // nothing has to be resolved and no listing request is issued.
        return Deletions.deleteAll(ids, "Dataset item id", Optional::of, this::delete, deleteConcurrency());
    }

    // Langfuse answers a delete with a DeleteDatasetItemResponse rather than 204. The engine wants a
    // Consumer, so the response is simply not read: there is nothing in it this layer reports.
    private void delete(String id) {
        this.datasetItemsApi.datasetItemsDelete(APIDatasetItemsDeleteRequest.newBuilder()
                .id(id)
                .build());
    }

    private static PagedResult<DatasetItem> fetch(DatasetItemsApi datasetItemsApi, Page page, DatasetItemFilter filter) {
        var builder = APIDatasetItemsListRequest.newBuilder()
                .page(page.index())
                .limit(page.size());

        filter.datasetName().ifPresent(builder::datasetName);
        filter.sourceTraceId().ifPresent(builder::sourceTraceId);
        filter.sourceObservationId().ifPresent(builder::sourceObservationId);
        filter.version().ifPresent(builder::version);

        var response = datasetItemsApi.datasetItemsList(builder.build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
