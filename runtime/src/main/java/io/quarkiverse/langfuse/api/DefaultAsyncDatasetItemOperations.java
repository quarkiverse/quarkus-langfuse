package io.quarkiverse.langfuse.api;

import java.util.Collection;

import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsCreateRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsDeleteRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsGetRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsListRequest;
import com.langfuse.api.datasetItems.async.DatasetItemsApi;
import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.DatasetItem;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncDatasetItemOperations extends AbstractAsyncPagedOperations<DatasetItem>
        implements AsyncDatasetItemOperations {
    private final DatasetItemsApi datasetItemsApi;
    private final LangfuseConfig config;

    DefaultAsyncDatasetItemOperations(DatasetItemsApi datasetItemsApi, LangfuseConfig config) {
        this(datasetItemsApi, config, DatasetItemFilter.none());
    }

    // The filter is captured in the fetcher lambda handed to the engine, which is why a filtered view
    // needs neither a widened AsyncPageFetcher nor a change to AsyncPagination: a view is just this
    // class constructed over a different fetcher.
    private DefaultAsyncDatasetItemOperations(DatasetItemsApi datasetItemsApi, LangfuseConfig config,
            DatasetItemFilter filter) {
        super(page -> fetch(datasetItemsApi, page, filter), config);
        this.datasetItemsApi = datasetItemsApi;
        this.config = config;
    }

    @Override
    public AsyncDatasetItemOperations matching(DatasetItemFilter filter) {
        return new DefaultAsyncDatasetItemOperations(this.datasetItemsApi, this.config,
                ValidationUtils.ensureNotNull(filter, "Filter"));
    }

    @Override
    public Uni<DatasetItem> findById(String id) {
        // Validated here rather than inside the completionStage supplier: a throw in there becomes a
        // failure event, and blank input must surface as a thrown IllegalArgumentException.
        var itemId = ValidationUtils.ensureNotBlank(id, "Dataset item id");

        // recoverWithNull is scoped to LangfuseNotFoundException alone: every other failure, a 401
        // included, must still fail the Uni rather than read as absence.
        return Uni.createFrom()
                .completionStage(() -> this.datasetItemsApi.datasetItemsGet(APIDatasetItemsGetRequest.newBuilder()
                        .id(itemId)
                        .build()))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    @Override
    public Uni<DatasetItem> create(CreateDatasetItemRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        return Uni.createFrom()
                .completionStage(() -> this.datasetItemsApi.datasetItemsCreate(APIDatasetItemsCreateRequest.newBuilder()
                        .createDatasetItemRequest(request)
                        .build()));
    }

    @Override
    public Uni<DeletionResult> deleteById(Collection<String> ids) {
        // Validation runs before the deferred wrapper so malformed input throws from the call, as the
        // rest of this tree does. Inside the supplier it would surface as a failed Uni at subscription
        // instead. deleteConcurrency() stays inside, so the config is read per subscription.
        DeletionIdentifiers.validated(ids, "Dataset item id");

        // Uni.createFrom()::item is the identity resolver: the delete endpoint takes the item id
        // directly, so nothing has to be resolved and no listing request is issued.
        return Uni.createFrom()
                .deferred(() -> AsyncDeletions.deleteAll(ids, "Dataset item id", Uni.createFrom()::item, this::delete,
                        deleteConcurrency()));
    }

    // Langfuse answers a delete with a DeleteDatasetItemResponse rather than 204. The engine only
    // needs the Uni to complete, so the response is carried through untouched as Uni<?>.
    private Uni<?> delete(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.datasetItemsApi.datasetItemsDelete(APIDatasetItemsDeleteRequest.newBuilder()
                        .id(id)
                        .build()));
    }

    private static Uni<PagedResult<DatasetItem>> fetch(DatasetItemsApi datasetItemsApi, Page page,
            DatasetItemFilter filter) {
        var builder = APIDatasetItemsListRequest.newBuilder()
                .page(page.index())
                .limit(page.size());

        filter.datasetName().ifPresent(builder::datasetName);
        filter.sourceTraceId().ifPresent(builder::sourceTraceId);
        filter.sourceObservationId().ifPresent(builder::sourceObservationId);
        filter.version().ifPresent(builder::version);

        var request = builder.build();

        return Uni.createFrom()
                .completionStage(() -> datasetItemsApi.datasetItemsList(request))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
