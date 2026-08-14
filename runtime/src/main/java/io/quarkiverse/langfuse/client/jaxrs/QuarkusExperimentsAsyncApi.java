package io.quarkiverse.langfuse.client.jaxrs;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.experiments.ExperimentsApi.APIExperimentsListItemsRequest;
import com.langfuse.api.experiments.ExperimentsApi.APIExperimentsListRequest;
import com.langfuse.api.model.ExperimentItemsResponse;
import com.langfuse.api.model.ExperimentsResponse;

public interface QuarkusExperimentsAsyncApi extends com.langfuse.api.experiments.async.ExperimentsApi {

    /**
     * List experiments with cursor-based pagination.
     */
    @GET
    @Path("/api/public/experiments")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<ExperimentsResponse> experimentsList(
            @QueryParam("fromStartTime") OffsetDateTime fromStartTime,
            @QueryParam("fields") String fields,
            @QueryParam("limit") Integer limit,
            @QueryParam("scoreLimit") Integer scoreLimit,
            @QueryParam("cursor") String cursor,
            @QueryParam("toStartTime") OffsetDateTime toStartTime,
            @QueryParam("id") String id,
            @QueryParam("name") String name,
            @QueryParam("datasetId") String datasetId,
            @QueryParam("filter") String filter);

    /**
     * List experiments with cursor-based pagination.
     */
    @Override
    default CompletionStage<ExperimentsResponse> experimentsList(APIExperimentsListRequest apiRequest) {
        return experimentsList(apiRequest.fromStartTime(), apiRequest.fields(), apiRequest.limit(),
                apiRequest.scoreLimit(), apiRequest.cursor(), apiRequest.toStartTime(), apiRequest.id(),
                apiRequest.name(), apiRequest.datasetId(), apiRequest.filter());
    }

    /**
     * List experiment items with cursor-based pagination.
     */
    @GET
    @Path("/api/public/experiment-items")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<ExperimentItemsResponse> experimentsListItems(
            @QueryParam("fromStartTime") OffsetDateTime fromStartTime,
            @QueryParam("fields") String fields,
            @QueryParam("limit") Integer limit,
            @QueryParam("scoreLimit") Integer scoreLimit,
            @QueryParam("cursor") String cursor,
            @QueryParam("toStartTime") OffsetDateTime toStartTime,
            @QueryParam("experimentId") String experimentId,
            @QueryParam("experimentName") String experimentName,
            @QueryParam("experimentItemId") String experimentItemId,
            @QueryParam("datasetId") String datasetId,
            @QueryParam("filter") String filter);

    /**
     * List experiment items with cursor-based pagination.
     */
    @Override
    default CompletionStage<ExperimentItemsResponse> experimentsListItems(
            APIExperimentsListItemsRequest apiRequest) {
        return experimentsListItems(apiRequest.fromStartTime(), apiRequest.fields(), apiRequest.limit(),
                apiRequest.scoreLimit(), apiRequest.cursor(), apiRequest.toStartTime(), apiRequest.experimentId(),
                apiRequest.experimentName(), apiRequest.experimentItemId(), apiRequest.datasetId(),
                apiRequest.filter());
    }

}
