package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.UnstableCreateDashboardWidgetRequest;
import com.langfuse.api.model.UnstableDashboardWidget;
import com.langfuse.api.model.UnstableDashboardWidgetChartType;
import com.langfuse.api.model.UnstableDashboardWidgetMetric;
import com.langfuse.api.model.UnstableDashboardWidgetMetricAggregation;
import com.langfuse.api.model.UnstableDashboardWidgetView;
import com.langfuse.api.model.UnstableUpdateDashboardWidgetRequest;
import com.langfuse.api.unstableDashboardWidgets.UnstableDashboardWidgetsApi.APIUnstableDashboardWidgetsCreateRequest;
import com.langfuse.api.unstableDashboardWidgets.UnstableDashboardWidgetsApi.APIUnstableDashboardWidgetsDeleteRequest;
import com.langfuse.api.unstableDashboardWidgets.UnstableDashboardWidgetsApi.APIUnstableDashboardWidgetsGetRequest;
import com.langfuse.api.unstableDashboardWidgets.UnstableDashboardWidgetsApi.APIUnstableDashboardWidgetsListRequest;
import com.langfuse.api.unstableDashboardWidgets.UnstableDashboardWidgetsApi.APIUnstableDashboardWidgetsUpdateRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class UnstableDashboardWidgetsApiTest {

    private static final String WIDGET_NAME = "test-widget-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String UPDATED_WIDGET_NAME = "updated-" + WIDGET_NAME;
    private static String widgetId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void createWidget() {
        assertThat(client.unstableDashboardWidgets().unstableDashboardWidgetsCreate(
                APIUnstableDashboardWidgetsCreateRequest.newBuilder()
                        .unstableCreateDashboardWidgetRequest(UnstableCreateDashboardWidgetRequest.builder()
                                .name(WIDGET_NAME)
                                .description("Test widget description")
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
                    assertThat(widget.getName()).isEqualTo(WIDGET_NAME);
                    widgetId = widget.getId();
                });
    }

    @Test
    @Order(2)
    void getWidget() {
        assertThat(client.unstableDashboardWidgets().unstableDashboardWidgetsGet(
                APIUnstableDashboardWidgetsGetRequest.newBuilder()
                        .widgetId(widgetId)
                        .build()))
                .extracting(UnstableDashboardWidget::getId, UnstableDashboardWidget::getName)
                .containsExactly(widgetId, WIDGET_NAME);
    }

    @Test
    @Order(2)
    void listWidgets() {
        assertThat(client.unstableDashboardWidgets().unstableDashboardWidgetsList(
                APIUnstableDashboardWidgetsListRequest.newBuilder()
                        .build()))
                .satisfies(list -> assertThat(list.getData())
                        .isNotEmpty()
                        .anyMatch(w -> widgetId.equals(w.getId())));
    }

    @Test
    @Order(3)
    void updateWidget() {
        assertThat(client.unstableDashboardWidgets().unstableDashboardWidgetsUpdate(
                APIUnstableDashboardWidgetsUpdateRequest.newBuilder()
                        .widgetId(widgetId)
                        .unstableUpdateDashboardWidgetRequest(UnstableUpdateDashboardWidgetRequest.builder()
                                .name(UPDATED_WIDGET_NAME)
                                .description("Updated widget description")
                                .build())
                        .build()))
                .extracting(UnstableDashboardWidget::getId, UnstableDashboardWidget::getName,
                        UnstableDashboardWidget::getDescription)
                .containsExactly(widgetId, UPDATED_WIDGET_NAME, "Updated widget description");
    }

    @Test
    @Order(4)
    void deleteWidget() {
        assertThat(client.unstableDashboardWidgets().unstableDashboardWidgetsDelete(
                APIUnstableDashboardWidgetsDeleteRequest.newBuilder()
                        .widgetId(widgetId)
                        .build()))
                .isNotNull();
    }
}
