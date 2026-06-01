package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsCreateRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsDeleteRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsGetRequest;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsListRequest;
import com.langfuse.api.model.DatasetItem;
import com.langfuse.api.model.DeleteDatasetItemResponse;
import com.langfuse.api.model.PaginatedDatasetItems;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "DatasetItems")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface DatasetItemsAsyncApi extends com.langfuse.api.datasetItems.async.DatasetItemsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetItemsCreate", method = "POST", path = "/api/public/dataset-items")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/dataset-items")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetItems_create")
    @Override
    public CompletionStage<DatasetItem> datasetItemsCreate(
            APIDatasetItemsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetItemsDelete", method = "DELETE", path = "/api/public/dataset-items/{id}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/dataset-items/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetItems_delete")
    @Override
    public CompletionStage<DeleteDatasetItemResponse> datasetItemsDelete(
            APIDatasetItemsDeleteRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetItemsGet", method = "GET", path = "/api/public/dataset-items/{id}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/dataset-items/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetItems_get")
    @Override
    public CompletionStage<DatasetItem> datasetItemsGet(
            APIDatasetItemsGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetItemsList", method = "GET", path = "/api/public/dataset-items")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/dataset-items")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetItems_list")
    @Override
    public CompletionStage<PaginatedDatasetItems> datasetItemsList(
            APIDatasetItemsListRequest apiRequest);

}
