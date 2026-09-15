package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.datasets.DatasetsApi;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsCreateRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsGetRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsListRequest;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.Dataset;

import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;

final class DefaultDatasetOperations extends AbstractPagedOperations<Dataset> implements DatasetOperations {
    private final DatasetsApi datasetsApi;

    DefaultDatasetOperations(DatasetsApi datasetsApi, LangfuseConfig config) {
        super(page -> fetch(datasetsApi, page), config);
        this.datasetsApi = datasetsApi;
    }

    @Override
    public Optional<Dataset> findByName(String datasetName) {
        var name = Names.require(datasetName, "Dataset name");

        // Direct lookup, not scanForName: Langfuse can resolve a dataset by name in one request
        // (datasetsGet takes the name directly), so this domain never has to walk the collection the
        // way the others do. The try/catch is still the only place absence is distinguished from
        // failure - everything other than a 404 propagates.
        try {
            return Optional.of(this.datasetsApi.datasetsGet(APIDatasetsGetRequest.newBuilder()
                    .datasetName(name)
                    .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Dataset createIfAbsent(CreateDatasetRequest request) {
        return findByName(request.getName())
                .orElseGet(() -> this.datasetsApi.datasetsCreate(APIDatasetsCreateRequest.newBuilder()
                        .createDatasetRequest(request)
                        .build()));
    }

    private static PagedResult<Dataset> fetch(DatasetsApi datasetsApi, Page page) {
        var response = datasetsApi.datasetsList(APIDatasetsListRequest.newBuilder()
                .page(page.index())
                .limit(page.size())
                .build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
