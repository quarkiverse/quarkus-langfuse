package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

/**
 * Integration tests for the Datasets API.
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class DatasetsApiTest {

    private static final String DATASET_NAME = "test-dataset-" + UUID.randomUUID();
    private static final String RUN_NAME = "test-run-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TRACE_ID = UUID.randomUUID().toString();
    private static String datasetId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void createDataset() {
        assertThat(client.datasets().datasetsCreate(
                DatasetsApi.APIDatasetsCreateRequest.newBuilder()
                        .createDatasetRequest(CreateDatasetRequest.builder()
                                .name(DATASET_NAME)
                                .description("Test dataset for integration tests")
                                .build())
                        .build()))
                .satisfies(dataset -> {
                    assertThat(dataset.getId()).isNotBlank();
                    assertThat(dataset.getName()).isEqualTo(DATASET_NAME);
                    assertThat(dataset.getDescription()).isEqualTo("Test dataset for integration tests");
                    assertThat(dataset.getProjectId()).isNotBlank();
                    assertThat(dataset.getCreatedAt()).isNotNull();
                    assertThat(dataset.getUpdatedAt()).isNotNull();
                    datasetId = dataset.getId();
                });
    }

    @Test
    @Order(2)
    void getDatasetByName() {
        assertThat(client.datasets().datasetsGet(
                DatasetsApi.APIDatasetsGetRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .build()))
                .extracting(Dataset::getId, Dataset::getName, Dataset::getDescription)
                .containsExactly(datasetId, DATASET_NAME, "Test dataset for integration tests");
    }

    @Test
    @Order(2)
    void listDatasetsContainsCreated() {
        assertThat(client.datasets().datasetsList(
                DatasetsApi.APIDatasetsListRequest.newBuilder()
                        .build()))
                .satisfies(datasets -> {
                    assertThat(datasets.getData())
                            .isNotEmpty()
                            .anyMatch(d -> DATASET_NAME.equals(d.getName()));
                    assertThat(datasets.getMeta().getTotalItems()).isGreaterThan(0);
                });
    }

    @Test
    @Order(3)
    void createRunForDataset() {
        var item = client.datasetItems().datasetItemsCreate(
                DatasetItemsApi.APIDatasetItemsCreateRequest.newBuilder()
                        .createDatasetItemRequest(CreateDatasetItemRequest.builder()
                                .datasetName(DATASET_NAME)
                                .input(Map.of("question", "test"))
                                .build())
                        .build());

        OtelTestHelper.ingestTrace(client, TRACE_ID, "datasets-run-test-trace");

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
        assertThatThrownBy(() -> client.datasets().datasetsGetRuns(
                DatasetsApi.APIDatasetsGetRunsRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    @Order(4)
    void getRunByNameReturns404InEventsOnlyMode() {
        assertThatThrownBy(() -> client.datasets().datasetsGetRun(
                DatasetsApi.APIDatasetsGetRunRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .runName(RUN_NAME)
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    @Order(5)
    void deleteRunReturns404InEventsOnlyMode() {
        assertThatThrownBy(() -> client.datasets().datasetsDeleteRun(
                DatasetsApi.APIDatasetsDeleteRunRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .runName(RUN_NAME)
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(404));
    }
}
