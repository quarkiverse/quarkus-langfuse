package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsCreateRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsDeleteRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsGetRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsListRequest;
import com.langfuse.api.model.DatasetItem;
import com.langfuse.api.model.DeleteDatasetItemResponse;
import com.langfuse.api.model.PaginatedDatasetItems;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "DatasetItems")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface DatasetItemsApi extends com.langfuse.api.datasetItems.DatasetItemsApi {

    /**
     * Create a dataset item
     *
     * @param apiRequest {@link APIDatasetItemsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetItemsCreate", method = "POST", path = "/api/public/dataset-items")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/dataset-items")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetItems_create")
    @Override
    public DatasetItem datasetItemsCreate(
            APIDatasetItemsCreateRequest apiRequest);

    /**
     * Delete a dataset item and all its run items.
     *
     * @param apiRequest {@link APIDatasetItemsDeleteRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetItemsDelete", method = "DELETE", path = "/api/public/dataset-items/{id}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/dataset-items/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetItems_delete")
    @Override
    public DeleteDatasetItemResponse datasetItemsDelete(
            APIDatasetItemsDeleteRequest apiRequest);

    /**
     * Get a dataset item
     *
     * @param apiRequest {@link APIDatasetItemsGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetItemsGet", method = "GET", path = "/api/public/dataset-items/{id}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/dataset-items/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetItems_get")
    @Override
    public DatasetItem datasetItemsGet(
            APIDatasetItemsGetRequest apiRequest);

    /**
     * Get dataset items.
     *
     * @param apiRequest {@link APIDatasetItemsListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetItemsList", method = "GET", path = "/api/public/dataset-items")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/dataset-items")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetItems_list")
    @Override
    public PaginatedDatasetItems datasetItemsList(
            APIDatasetItemsListRequest apiRequest);

}
