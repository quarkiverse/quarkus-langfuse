package io.quarkiverse.langfuse.client.jaxrs;

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

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Datasets")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface DatasetsApi extends com.langfuse.api.datasets.DatasetsApi {

    /**
     * Create a dataset
     *
     * @param apiRequest {@link APIDatasetsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsCreate", method = "POST", path = "/api/public/v2/datasets")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/v2/datasets")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_create")
    @Override
    public Dataset datasetsCreate(
            APIDatasetsCreateRequest apiRequest);

    /**
     * Delete a dataset run and all its run items.
     *
     * @param apiRequest {@link APIDatasetsDeleteRunRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsDeleteRun", method = "DELETE", path = "/api/public/datasets/{datasetName}/runs/{runName}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/datasets/{datasetName}/runs/{runName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_deleteRun")
    @Override
    public DeleteDatasetRunResponse datasetsDeleteRun(
            APIDatasetsDeleteRunRequest apiRequest);

    /**
     * Get a dataset
     *
     * @param apiRequest {@link APIDatasetsGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsGet", method = "GET", path = "/api/public/v2/datasets/{datasetName}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/datasets/{datasetName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_get")
    @Override
    public Dataset datasetsGet(
            APIDatasetsGetRequest apiRequest);

    /**
     * Get a dataset run and its items
     *
     * @param apiRequest {@link APIDatasetsGetRunRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsGetRun", method = "GET", path = "/api/public/datasets/{datasetName}/runs/{runName}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/datasets/{datasetName}/runs/{runName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_getRun")
    @Override
    public DatasetRunWithItems datasetsGetRun(
            APIDatasetsGetRunRequest apiRequest);

    /**
     * Get dataset runs
     *
     * @param apiRequest {@link APIDatasetsGetRunsRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsGetRuns", method = "GET", path = "/api/public/datasets/{datasetName}/runs")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/datasets/{datasetName}/runs")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_getRuns")
    @Override
    public PaginatedDatasetRuns datasetsGetRuns(
            APIDatasetsGetRunsRequest apiRequest);

    /**
     * Get all datasets
     *
     * @param apiRequest {@link APIDatasetsListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetsList", method = "GET", path = "/api/public/v2/datasets")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/datasets")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasets_list")
    @Override
    public PaginatedDatasets datasetsList(
            APIDatasetsListRequest apiRequest);

}
