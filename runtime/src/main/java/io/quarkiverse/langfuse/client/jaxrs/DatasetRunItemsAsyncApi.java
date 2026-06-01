package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.datasetRunItems.DatasetRunItemsApi.APIDatasetRunItemsCreateRequest;
import com.langfuse.api.datasetRunItems.DatasetRunItemsApi.APIDatasetRunItemsListRequest;
import com.langfuse.api.model.DatasetRunItem;
import com.langfuse.api.model.PaginatedDatasetRunItems;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "DatasetRunItems")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface DatasetRunItemsAsyncApi extends com.langfuse.api.datasetRunItems.async.DatasetRunItemsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetRunItemsCreate", method = "POST", path = "/api/public/dataset-run-items")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/dataset-run-items")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetRunItems_create")
    @Override
    public CompletionStage<DatasetRunItem> datasetRunItemsCreate(
            APIDatasetRunItemsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "datasetRunItemsList", method = "GET", path = "/api/public/dataset-run-items")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/dataset-run-items")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("datasetRunItems_list")
    @Override
    public CompletionStage<PaginatedDatasetRunItems> datasetRunItemsList(
            APIDatasetRunItemsListRequest apiRequest);

}
