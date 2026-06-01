package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.datasetRunItems.DatasetRunItemsApi.APIDatasetRunItemsCreateRequest;
import com.langfuse.api.datasetRunItems.DatasetRunItemsApi.APIDatasetRunItemsListRequest;
import com.langfuse.api.model.DatasetRunItem;
import com.langfuse.api.model.PaginatedDatasetRunItems;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "DatasetRunItems")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface DatasetRunItemsApi extends com.langfuse.api.datasetRunItems.DatasetRunItemsApi {

    /**
     * Create a dataset run item
     *
     * @param apiRequest {@link APIDatasetRunItemsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetRunItemsCreate", method = "POST", path = "/api/public/dataset-run-items")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/dataset-run-items")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetRunItems_create")
    @Override
    public DatasetRunItem datasetRunItemsCreate(
            APIDatasetRunItemsCreateRequest apiRequest);

    /**
     * List dataset run items
     *
     * @param apiRequest {@link APIDatasetRunItemsListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetRunItemsList", method = "GET", path = "/api/public/dataset-run-items")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/dataset-run-items")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetRunItems_list")
    @Override
    public PaginatedDatasetRunItems datasetRunItemsList(
            APIDatasetRunItemsListRequest apiRequest);

}
