package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.datasets.DatasetsApi.APIDatasetsCreateRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsDeleteRunRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsGetRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsGetRunRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsGetRunsRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsListRequest;
import com.langfuse.api.model.Dataset;
import com.langfuse.api.model.DatasetRunWithItems;
import com.langfuse.api.model.DeleteDatasetRunResponse;
import com.langfuse.api.model.PaginatedDatasetRuns;
import com.langfuse.api.model.PaginatedDatasets;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Datasets")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface DatasetsAsyncApi extends com.langfuse.api.datasets.async.DatasetsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsCreate", method = "POST", path = "/api/public/v2/datasets")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/v2/datasets")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_create")
    @Override
    public CompletionStage<Dataset> datasetsCreate(
            APIDatasetsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsDeleteRun", method = "DELETE", path = "/api/public/datasets/{datasetName}/runs/{runName}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/datasets/{datasetName}/runs/{runName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_deleteRun")
    @Override
    public CompletionStage<DeleteDatasetRunResponse> datasetsDeleteRun(
            APIDatasetsDeleteRunRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsGet", method = "GET", path = "/api/public/v2/datasets/{datasetName}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/datasets/{datasetName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_get")
    @Override
    public CompletionStage<Dataset> datasetsGet(
            APIDatasetsGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsGetRun", method = "GET", path = "/api/public/datasets/{datasetName}/runs/{runName}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/datasets/{datasetName}/runs/{runName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_getRun")
    @Override
    public CompletionStage<DatasetRunWithItems> datasetsGetRun(
            APIDatasetsGetRunRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsGetRuns", method = "GET", path = "/api/public/datasets/{datasetName}/runs")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/datasets/{datasetName}/runs")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_getRuns")
    @Override
    public CompletionStage<PaginatedDatasetRuns> datasetsGetRuns(
            APIDatasetsGetRunsRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsList", method = "GET", path = "/api/public/v2/datasets")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/datasets")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_list")
    @Override
    public CompletionStage<PaginatedDatasets> datasetsList(
            APIDatasetsListRequest apiRequest);

}
