package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.UnstableCreateDashboardPlacementRequest;
import com.langfuse.api.model.UnstableCreateDashboardRequest;
import com.langfuse.api.model.UnstableDashboard;
import com.langfuse.api.model.UnstableDashboardList;
import com.langfuse.api.model.UnstableDashboardPlacement;
import com.langfuse.api.model.UnstableDeleteDashboardPlacementResponse;
import com.langfuse.api.model.UnstableDeleteDashboardResponse;
import com.langfuse.api.model.UnstableUpdateDashboardPlacementRequest;
import com.langfuse.api.model.UnstableUpdateDashboardRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsAddPlacementRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsCreateRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsDeletePlacementRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsDeleteRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsGetRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsListRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsUpdatePlacementRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsUpdateRequest;

public interface QuarkusUnstableDashboardsAsyncApi
        extends com.langfuse.api.unstableDashboards.async.UnstableDashboardsApi {

    /**
     * Add a placement to a dashboard.
     */
    @POST
    @Path("/api/public/unstable/dashboards/{dashboardId}/placements")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDashboardPlacement> unstableDashboardsAddPlacement(
            @PathParam("dashboardId") String dashboardId,
            UnstableCreateDashboardPlacementRequest unstableCreateDashboardPlacementRequest);

    /**
     * Add a placement to a dashboard.
     */
    @Override
    default CompletionStage<UnstableDashboardPlacement> unstableDashboardsAddPlacement(
            APIUnstableDashboardsAddPlacementRequest apiRequest) {
        return unstableDashboardsAddPlacement(apiRequest.dashboardId(),
                apiRequest.unstableCreateDashboardPlacementRequest());
    }

    /**
     * Create a dashboard.
     */
    @POST
    @Path("/api/public/unstable/dashboards")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDashboard> unstableDashboardsCreate(
            UnstableCreateDashboardRequest unstableCreateDashboardRequest);

    /**
     * Create a dashboard.
     */
    @Override
    default CompletionStage<UnstableDashboard> unstableDashboardsCreate(
            APIUnstableDashboardsCreateRequest apiRequest) {
        return unstableDashboardsCreate(apiRequest.unstableCreateDashboardRequest());
    }

    /**
     * Delete a dashboard.
     */
    @DELETE
    @Path("/api/public/unstable/dashboards/{dashboardId}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDeleteDashboardResponse> unstableDashboardsDelete(
            @PathParam("dashboardId") String dashboardId);

    /**
     * Delete a dashboard.
     */
    @Override
    default CompletionStage<UnstableDeleteDashboardResponse> unstableDashboardsDelete(
            APIUnstableDashboardsDeleteRequest apiRequest) {
        return unstableDashboardsDelete(apiRequest.dashboardId());
    }

    /**
     * Remove a placement from a dashboard.
     */
    @DELETE
    @Path("/api/public/unstable/dashboards/{dashboardId}/placements/{placementId}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDeleteDashboardPlacementResponse> unstableDashboardsDeletePlacement(
            @PathParam("dashboardId") String dashboardId,
            @PathParam("placementId") String placementId);

    /**
     * Remove a placement from a dashboard.
     */
    @Override
    default CompletionStage<UnstableDeleteDashboardPlacementResponse> unstableDashboardsDeletePlacement(
            APIUnstableDashboardsDeletePlacementRequest apiRequest) {
        return unstableDashboardsDeletePlacement(apiRequest.dashboardId(), apiRequest.placementId());
    }

    /**
     * Get a dashboard by id.
     */
    @GET
    @Path("/api/public/unstable/dashboards/{dashboardId}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDashboard> unstableDashboardsGet(
            @PathParam("dashboardId") String dashboardId);

    /**
     * Get a dashboard by id.
     */
    @Override
    default CompletionStage<UnstableDashboard> unstableDashboardsGet(
            APIUnstableDashboardsGetRequest apiRequest) {
        return unstableDashboardsGet(apiRequest.dashboardId());
    }

    /**
     * List dashboards in the project.
     */
    @GET
    @Path("/api/public/unstable/dashboards")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDashboardList> unstableDashboardsList(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * List dashboards in the project.
     */
    @Override
    default CompletionStage<UnstableDashboardList> unstableDashboardsList(
            APIUnstableDashboardsListRequest apiRequest) {
        return unstableDashboardsList(apiRequest.page(), apiRequest.limit());
    }

    /**
     * Update a dashboard.
     */
    @PATCH
    @Path("/api/public/unstable/dashboards/{dashboardId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDashboard> unstableDashboardsUpdate(
            @PathParam("dashboardId") String dashboardId,
            UnstableUpdateDashboardRequest unstableUpdateDashboardRequest);

    /**
     * Update a dashboard.
     */
    @Override
    default CompletionStage<UnstableDashboard> unstableDashboardsUpdate(
            APIUnstableDashboardsUpdateRequest apiRequest) {
        return unstableDashboardsUpdate(apiRequest.dashboardId(), apiRequest.unstableUpdateDashboardRequest());
    }

    /**
     * Update a placement on a dashboard.
     */
    @PATCH
    @Path("/api/public/unstable/dashboards/{dashboardId}/placements/{placementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDashboardPlacement> unstableDashboardsUpdatePlacement(
            @PathParam("dashboardId") String dashboardId,
            @PathParam("placementId") String placementId,
            UnstableUpdateDashboardPlacementRequest unstableUpdateDashboardPlacementRequest);

    /**
     * Update a placement on a dashboard.
     */
    @Override
    default CompletionStage<UnstableDashboardPlacement> unstableDashboardsUpdatePlacement(
            APIUnstableDashboardsUpdatePlacementRequest apiRequest) {
        return unstableDashboardsUpdatePlacement(apiRequest.dashboardId(), apiRequest.placementId(),
                apiRequest.unstableUpdateDashboardPlacementRequest());
    }

}
