package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.datasets.DatasetsApi.APIDatasetsCreateRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsGetRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsListRequest;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.Dataset;
import com.langfuse.api.model.PaginatedDatasets;

/**
 * Langfuse Datasets Async API
 */
public interface QuarkusDatasetsAsyncApi extends com.langfuse.api.datasets.async.DatasetsApi {

    /**
     * Create a dataset
     */
    @POST
    @Path("/api/public/v2/datasets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Dataset> datasetsCreate(
            CreateDatasetRequest createDatasetRequest);

    /**
     * Create a dataset
     */
    @Override
    default CompletionStage<Dataset> datasetsCreate(APIDatasetsCreateRequest apiRequest) {
        return datasetsCreate(apiRequest.createDatasetRequest());
    }

    /**
     * Get a dataset
     */
    @GET
    @Path("/api/public/v2/datasets/{datasetName}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Dataset> datasetsGet(
            @PathParam("datasetName") String datasetName);

    /**
     * Get a dataset
     */
    @Override
    default CompletionStage<Dataset> datasetsGet(APIDatasetsGetRequest apiRequest) {
        return datasetsGet(apiRequest.datasetName());
    }

    /**
     * Get all datasets
     */
    @GET
    @Path("/api/public/v2/datasets")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<PaginatedDatasets> datasetsList(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * Get all datasets
     */
    @Override
    default CompletionStage<PaginatedDatasets> datasetsList(APIDatasetsListRequest apiRequest) {
        return datasetsList(apiRequest.page(), apiRequest.limit());
    }

}
