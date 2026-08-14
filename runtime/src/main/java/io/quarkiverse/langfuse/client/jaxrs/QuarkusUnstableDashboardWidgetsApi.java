package io.quarkiverse.langfuse.client.jaxrs;

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

import com.langfuse.api.model.UnstableCreateDashboardWidgetRequest;
import com.langfuse.api.model.UnstableDashboardWidget;
import com.langfuse.api.model.UnstableDashboardWidgetList;
import com.langfuse.api.model.UnstableDeleteDashboardWidgetResponse;
import com.langfuse.api.model.UnstableUpdateDashboardWidgetRequest;

/**
 * Langfuse Unstable Dashboard Widgets API
 */
public interface QuarkusUnstableDashboardWidgetsApi
        extends com.langfuse.api.unstableDashboardWidgets.UnstableDashboardWidgetsApi {

    /**
     * Create a dashboard widget.
     */
    @POST
    @Path("/api/public/unstable/dashboard-widgets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    UnstableDashboardWidget unstableDashboardWidgetsCreate(
            UnstableCreateDashboardWidgetRequest unstableCreateDashboardWidgetRequest);

    /**
     * Create a dashboard widget.
     */
    @Override
    default UnstableDashboardWidget unstableDashboardWidgetsCreate(
            APIUnstableDashboardWidgetsCreateRequest apiRequest) {
        return unstableDashboardWidgetsCreate(apiRequest.unstableCreateDashboardWidgetRequest());
    }

    /**
     * Delete a dashboard widget.
     */
    @DELETE
    @Path("/api/public/unstable/dashboard-widgets/{widgetId}")
    @Produces(MediaType.APPLICATION_JSON)
    UnstableDeleteDashboardWidgetResponse unstableDashboardWidgetsDelete(
            @PathParam("widgetId") String widgetId);

    /**
     * Delete a dashboard widget.
     */
    @Override
    default UnstableDeleteDashboardWidgetResponse unstableDashboardWidgetsDelete(
            APIUnstableDashboardWidgetsDeleteRequest apiRequest) {
        return unstableDashboardWidgetsDelete(apiRequest.widgetId());
    }

    /**
     * Get a dashboard widget by id.
     */
    @GET
    @Path("/api/public/unstable/dashboard-widgets/{widgetId}")
    @Produces(MediaType.APPLICATION_JSON)
    UnstableDashboardWidget unstableDashboardWidgetsGet(
            @PathParam("widgetId") String widgetId);

    /**
     * Get a dashboard widget by id.
     */
    @Override
    default UnstableDashboardWidget unstableDashboardWidgetsGet(
            APIUnstableDashboardWidgetsGetRequest apiRequest) {
        return unstableDashboardWidgetsGet(apiRequest.widgetId());
    }

    /**
     * List dashboard widgets in the project.
     */
    @GET
    @Path("/api/public/unstable/dashboard-widgets")
    @Produces(MediaType.APPLICATION_JSON)
    UnstableDashboardWidgetList unstableDashboardWidgetsList(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * List dashboard widgets in the project.
     */
    @Override
    default UnstableDashboardWidgetList unstableDashboardWidgetsList(
            APIUnstableDashboardWidgetsListRequest apiRequest) {
        return unstableDashboardWidgetsList(apiRequest.page(), apiRequest.limit());
    }

    /**
     * Update a dashboard widget.
     */
    @PATCH
    @Path("/api/public/unstable/dashboard-widgets/{widgetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    UnstableDashboardWidget unstableDashboardWidgetsUpdate(
            @PathParam("widgetId") String widgetId,
            UnstableUpdateDashboardWidgetRequest unstableUpdateDashboardWidgetRequest);

    /**
     * Update a dashboard widget.
     */
    @Override
    default UnstableDashboardWidget unstableDashboardWidgetsUpdate(
            APIUnstableDashboardWidgetsUpdateRequest apiRequest) {
        return unstableDashboardWidgetsUpdate(apiRequest.widgetId(),
                apiRequest.unstableUpdateDashboardWidgetRequest());
    }

}
