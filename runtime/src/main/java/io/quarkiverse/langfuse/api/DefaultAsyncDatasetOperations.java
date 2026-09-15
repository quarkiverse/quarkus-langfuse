package io.quarkiverse.langfuse.api;

import com.langfuse.api.datasets.DatasetsApi.APIDatasetsCreateRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsGetRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsListRequest;
import com.langfuse.api.datasets.async.DatasetsApi;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.Dataset;

import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncDatasetOperations extends AbstractAsyncPagedOperations<Dataset> implements AsyncDatasetOperations {
    private final DatasetsApi datasetsApi;

    DefaultAsyncDatasetOperations(DatasetsApi datasetsApi, LangfuseConfig config) {
        super(page -> fetch(datasetsApi, page), config);
        this.datasetsApi = datasetsApi;
    }

    @Override
    public Uni<Dataset> findByName(String datasetName) {
        var name = Names.require(datasetName, "Dataset name");

        // Direct lookup, not scanForName: Langfuse can resolve a dataset by name in one request, so
        // this domain never walks the collection the way the others have to.
        return Uni.createFrom()
                .completionStage(() -> this.datasetsApi.datasetsGet(APIDatasetsGetRequest.newBuilder()
                        .datasetName(name)
                        .build()))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    @Override
    public Uni<Dataset> createIfAbsent(CreateDatasetRequest request) {
        // See DefaultAsyncModelOperations.createIfAbsent for why flatMap + ternary.
        return findByName(request.getName())
                .flatMap(existing -> (existing != null)
                        ? Uni.createFrom().item(existing)
                        : Uni.createFrom()
                                .completionStage(() -> this.datasetsApi.datasetsCreate(APIDatasetsCreateRequest.newBuilder()
                                        .createDatasetRequest(request)
                                        .build())));
    }

    private static Uni<PagedResult<Dataset>> fetch(DatasetsApi datasetsApi, Page page) {
        return Uni.createFrom()
                .completionStage(() -> datasetsApi.datasetsList(APIDatasetsListRequest.newBuilder()
                        .page(page.index())
                        .limit(page.size())
                        .build()))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
