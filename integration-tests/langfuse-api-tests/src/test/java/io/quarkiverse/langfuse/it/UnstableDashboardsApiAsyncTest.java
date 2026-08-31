package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.UnstableCreateDashboardRequest;
import com.langfuse.api.model.UnstableCreateDashboardWidgetRequest;
import com.langfuse.api.model.UnstableCreateWidgetPlacement;
import com.langfuse.api.model.UnstableDashboardWidgetChartType;
import com.langfuse.api.model.UnstableDashboardWidgetMetric;
import com.langfuse.api.model.UnstableDashboardWidgetMetricAggregation;
import com.langfuse.api.model.UnstableDashboardWidgetView;
import com.langfuse.api.model.UnstableUpdateDashboardPlacementRequest;
import com.langfuse.api.model.UnstableUpdateDashboardRequest;
import com.langfuse.api.unstableDashboardWidgets.UnstableDashboardWidgetsApi.APIUnstableDashboardWidgetsCreateRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsAddPlacementRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsCreateRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsDeletePlacementRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsDeleteRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsGetRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsListRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsUpdatePlacementRequest;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi.APIUnstableDashboardsUpdateRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class UnstableDashboardsApiAsyncTest {

    private static final String DASHBOARD_NAME = "async-test-dashboard-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String UPDATED_DASHBOARD_NAME = "updated-" + DASHBOARD_NAME;
    private static String dashboardId;
    private static String widgetId;
    private static String placementId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void createDashboard() {
        assertThat(client.asyncUnstableDashboards().unstableDashboardsCreate(
                APIUnstableDashboardsCreateRequest.newBuilder()
                        .unstableCreateDashboardRequest(UnstableCreateDashboardRequest.builder()
                                .name(DASHBOARD_NAME)
                                .description("Async test dashboard description")
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(dashboard -> {
                    assertThat(dashboard.getId()).isNotBlank();
                    assertThat(dashboard.getName()).isEqualTo(DASHBOARD_NAME);
                    dashboardId = dashboard.getId();
                });
    }

    @Test
    @Order(1)
    void createWidgetForPlacement() {
        // Create a widget to place on the dashboard (sync — setup step)
        assertThat(client.unstableDashboardWidgets().unstableDashboardWidgetsCreate(
                APIUnstableDashboardWidgetsCreateRequest.newBuilder()
                        .unstableCreateDashboardWidgetRequest(UnstableCreateDashboardWidgetRequest.builder()
                                .name("async-placement-test-widget")
                                .view(UnstableDashboardWidgetView.OBSERVATIONS)
                                .dimensions(List.of())
                                .metrics(List.of(UnstableDashboardWidgetMetric.builder()
                                        .measure("latency")
                                        .agg(UnstableDashboardWidgetMetricAggregation.AVG)
                                        .build()))
                                .filters(List.of())
                                .chartType(UnstableDashboardWidgetChartType.LINE_TIME_SERIES)
                                .build())
                        .build()))
                .satisfies(widget -> {
                    assertThat(widget.getId()).isNotBlank();
                    widgetId = widget.getId();
                });
    }

    @Test
    @Order(2)
    void getDashboard() {
        assertThat(client.asyncUnstableDashboards().unstableDashboardsGet(
                APIUnstableDashboardsGetRequest.newBuilder()
                        .dashboardId(dashboardId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(dashboard -> assertThat(dashboard.getName()).isEqualTo(DASHBOARD_NAME));
    }

    @Test
    @Order(2)
    void listDashboards() {
        assertThat(client.asyncUnstableDashboards().unstableDashboardsList(
                APIUnstableDashboardsListRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(list -> assertThat(list.getData())
                        .isNotEmpty()
                        .anyMatch(d -> dashboardId.equals(d.getId())));
    }

    @Test
    @Order(3)
    void addPlacement() {
        // Place the widget on the dashboard grid
        assertThat(client.asyncUnstableDashboards().unstableDashboardsAddPlacement(
                APIUnstableDashboardsAddPlacementRequest.newBuilder()
                        .dashboardId(dashboardId)
                        .unstableCreateDashboardPlacementRequest(
                                new com.langfuse.api.model.UnstableCreateDashboardPlacementRequest(
                                        UnstableCreateWidgetPlacement.builder()
                                                .widgetId(widgetId)
                                                .x(0)
                                                .y(0)
                                                .width(6)
                                                .height(6)
                                                .type(UnstableCreateWidgetPlacement.TypeEnum.WIDGET)
                                                .build()))
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(placement -> {
                    assertThat(placement).isNotNull();
                    placementId = placement.getUnstableWidgetPlacement().getId();
                    assertThat(placementId).isNotBlank();
                });
    }

    @Test
    @Order(4)
    void updatePlacement() {
        // Move and resize the placement on the grid
        assertThat(client.asyncUnstableDashboards().unstableDashboardsUpdatePlacement(
                APIUnstableDashboardsUpdatePlacementRequest.newBuilder()
                        .dashboardId(dashboardId)
                        .placementId(placementId)
                        .unstableUpdateDashboardPlacementRequest(UnstableUpdateDashboardPlacementRequest.builder()
                                .x(2)
                                .y(2)
                                .width(8)
                                .height(4)
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(updated -> assertThat(updated).isNotNull());
    }

    @Test
    @Order(5)
    void deletePlacement() {
        assertThat(client.asyncUnstableDashboards().unstableDashboardsDeletePlacement(
                APIUnstableDashboardsDeletePlacementRequest.newBuilder()
                        .dashboardId(dashboardId)
                        .placementId(placementId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(response -> assertThat(response).isNotNull());
    }

    @Test
    @Order(6)
    void updateDashboard() {
        assertThat(client.asyncUnstableDashboards().unstableDashboardsUpdate(
                APIUnstableDashboardsUpdateRequest.newBuilder()
                        .dashboardId(dashboardId)
                        .unstableUpdateDashboardRequest(UnstableUpdateDashboardRequest.builder()
                                .name(UPDATED_DASHBOARD_NAME)
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(dashboard -> assertThat(dashboard.getName()).isEqualTo(UPDATED_DASHBOARD_NAME));
    }

    @Test
    @Order(7)
    void deleteDashboard() {
        assertThat(client.asyncUnstableDashboards().unstableDashboardsDelete(
                APIUnstableDashboardsDeleteRequest.newBuilder()
                        .dashboardId(dashboardId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }
}
