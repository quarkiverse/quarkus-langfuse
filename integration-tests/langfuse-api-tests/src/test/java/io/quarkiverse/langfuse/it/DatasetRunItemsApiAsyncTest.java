package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsCreateRequest;
import com.langfuse.api.datasetRunItems.DatasetRunItemsApi.APIDatasetRunItemsCreateRequest;
import com.langfuse.api.datasetRunItems.DatasetRunItemsApi.APIDatasetRunItemsListRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsCreateRequest;
import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.CreateDatasetRunItemRequest;
import com.langfuse.api.model.DatasetRunItem;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class DatasetRunItemsApiAsyncTest {

    private static final String DATASET_NAME = "async-test-run-items-dataset-" + UUID.randomUUID();
    private static final String RUN_NAME = "async-test-run-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static String datasetId;
    private static String datasetItemId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void setupDatasetAndTrace() {
        datasetId = client.datasets().datasetsCreate(
                APIDatasetsCreateRequest.newBuilder()
                        .createDatasetRequest(CreateDatasetRequest.builder()
                                .name(DATASET_NAME)
                                .build())
                        .build())
                .getId();

        var item = client.datasetItems().datasetItemsCreate(
                APIDatasetItemsCreateRequest.newBuilder()
                        .createDatasetItemRequest(CreateDatasetItemRequest.builder()
                                .datasetName(DATASET_NAME)
                                .input(Map.of("question", "What is async testing?"))
                                .build())
                        .build());

        assertThat(item.getId()).isNotBlank();
        datasetItemId = item.getId();

        // Ingest via OTel — legacy ingestion rejects trace-create in v4 events_only mode
        OtelTestHelper.ingestTrace(client, TRACE_ID, "async-run-items-test-trace");
    }

    @Test
    @Order(2)
    void createDatasetRunItem() {
        assertThat(client.asyncDatasetRunItems().datasetRunItemsCreate(
                APIDatasetRunItemsCreateRequest.newBuilder()
                        .createDatasetRunItemRequest(CreateDatasetRunItemRequest.builder()
                                .runName(RUN_NAME)
                                .datasetItemId(datasetItemId)
                                .traceId(TRACE_ID)
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(runItem -> {
                    assertThat(runItem.getId()).isNotBlank();
                    assertThat(runItem.getCreatedAt()).isNotNull();
                })
                .extracting(DatasetRunItem::getDatasetRunName, DatasetRunItem::getDatasetItemId, DatasetRunItem::getTraceId)
                .containsExactly(RUN_NAME, datasetItemId, TRACE_ID);
    }

    @Test
    @Order(3)
    void listDatasetRunItemsReturns404InEventsOnlyMode() {
        assertThat(client.asyncDatasetRunItems().datasetRunItemsList(
                APIDatasetRunItemsListRequest.newBuilder()
                        .datasetId(datasetId)
                        .runName(RUN_NAME)
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }
}
