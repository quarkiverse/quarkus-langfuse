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
import com.langfuse.api.datasetItems.DatasetItemsApi;
import com.langfuse.api.datasetRunItems.DatasetRunItemsApi;
import com.langfuse.api.datasets.DatasetsApi;
import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.CreateDatasetRunItemRequest;
import com.langfuse.api.model.Dataset;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class DatasetsApiAsyncTest {

    private static final String DATASET_NAME = "async-test-dataset-" + UUID.randomUUID();
    private static final String RUN_NAME = "async-test-run-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TRACE_ID = UUID.randomUUID().toString();
    private static String datasetId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void createDataset() {
        assertThat(client.asyncDatasets().datasetsCreate(
                DatasetsApi.APIDatasetsCreateRequest.newBuilder()
                        .createDatasetRequest(CreateDatasetRequest.builder()
                                .name(DATASET_NAME)
                                .description("Async test dataset")
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(dataset -> {
                    assertThat(dataset.getId()).isNotBlank();
                    assertThat(dataset.getName()).isEqualTo(DATASET_NAME);
                    datasetId = dataset.getId();
                });
    }

    @Test
    @Order(2)
    void getDatasetByName() {
        assertThat(client.asyncDatasets().datasetsGet(
                DatasetsApi.APIDatasetsGetRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .extracting(Dataset::getId, Dataset::getDescription)
                .containsExactly(datasetId, "Async test dataset");
    }

    @Test
    @Order(2)
    void listDatasetsContainsCreated() {
        assertThat(client.asyncDatasets().datasetsList(
                DatasetsApi.APIDatasetsListRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(datasets -> assertThat(datasets.getData())
                        .isNotEmpty()
                        .anyMatch(d -> DATASET_NAME.equals(d.getName())));
    }

    @Test
    @Order(3)
    void createRunForDataset() {
        var item = client.datasetItems().datasetItemsCreate(
                DatasetItemsApi.APIDatasetItemsCreateRequest.newBuilder()
                        .createDatasetItemRequest(CreateDatasetItemRequest.builder()
                                .datasetName(DATASET_NAME)
                                .input(Map.of("question", "async test"))
                                .build())
                        .build());

        OtelTestHelper.ingestTrace(client, TRACE_ID, "async-datasets-run-test-trace");

        client.datasetRunItems().datasetRunItemsCreate(
                DatasetRunItemsApi.APIDatasetRunItemsCreateRequest.newBuilder()
                        .createDatasetRunItemRequest(CreateDatasetRunItemRequest.builder()
                                .runName(RUN_NAME)
                                .datasetItemId(item.getId())
                                .traceId(TRACE_ID)
                                .build())
                        .build());
    }

    @Test
    @Order(4)
    void getRunsReturns404InEventsOnlyMode() {
        assertThat(client.asyncDatasets().datasetsGetRuns(
                DatasetsApi.APIDatasetsGetRunsRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }

    @Test
    @Order(4)
    void getRunByNameReturns404InEventsOnlyMode() {
        assertThat(client.asyncDatasets().datasetsGetRun(
                DatasetsApi.APIDatasetsGetRunRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .runName(RUN_NAME)
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }

    @Test
    @Order(5)
    void deleteRunReturns404InEventsOnlyMode() {
        assertThat(client.asyncDatasets().datasetsDeleteRun(
                DatasetsApi.APIDatasetsDeleteRunRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .runName(RUN_NAME)
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }
}
