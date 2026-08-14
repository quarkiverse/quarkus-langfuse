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
import com.langfuse.api.model.UnstableCreateDashboardWidgetRequest;
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
class UnstableDashboardWidgetsApiAsyncTest {

    private static final String WIDGET_NAME = "async-test-widget-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String UPDATED_WIDGET_NAME = "updated-" + WIDGET_NAME;
    private static String widgetId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void createWidget() {
        assertThat(client.asyncUnstableDashboardWidgets().unstableDashboardWidgetsCreate(
                APIUnstableDashboardWidgetsCreateRequest.newBuilder()
                        .unstableCreateDashboardWidgetRequest(UnstableCreateDashboardWidgetRequest.builder()
                                .name(WIDGET_NAME)
                                .description("Async test widget description")
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
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(widget -> {
                    assertThat(widget.getId()).isNotBlank();
                    assertThat(widget.getName()).isEqualTo(WIDGET_NAME);
                    widgetId = widget.getId();
                });
    }

    @Test
    @Order(2)
    void getWidget() {
        assertThat(client.asyncUnstableDashboardWidgets().unstableDashboardWidgetsGet(
                APIUnstableDashboardWidgetsGetRequest.newBuilder()
                        .widgetId(widgetId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(widget -> assertThat(widget.getName()).isEqualTo(WIDGET_NAME));
    }

    @Test
    @Order(2)
    void listWidgets() {
        assertThat(client.asyncUnstableDashboardWidgets().unstableDashboardWidgetsList(
                APIUnstableDashboardWidgetsListRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(list -> assertThat(list.getData())
                        .isNotEmpty()
                        .anyMatch(w -> widgetId.equals(w.getId())));
    }

    @Test
    @Order(3)
    void updateWidget() {
        assertThat(client.asyncUnstableDashboardWidgets().unstableDashboardWidgetsUpdate(
                APIUnstableDashboardWidgetsUpdateRequest.newBuilder()
                        .widgetId(widgetId)
                        .unstableUpdateDashboardWidgetRequest(UnstableUpdateDashboardWidgetRequest.builder()
                                .name(UPDATED_WIDGET_NAME)
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(widget -> assertThat(widget.getName()).isEqualTo(UPDATED_WIDGET_NAME));
    }

    @Test
    @Order(4)
    void deleteWidget() {
        assertThat(client.asyncUnstableDashboardWidgets().unstableDashboardWidgetsDelete(
                APIUnstableDashboardWidgetsDeleteRequest.newBuilder()
                        .widgetId(widgetId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }
}
