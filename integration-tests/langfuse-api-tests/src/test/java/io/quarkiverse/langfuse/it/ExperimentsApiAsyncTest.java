package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.datasetItems.DatasetItemsApi.APIDatasetItemsCreateRequest;
import com.langfuse.api.datasetRunItems.DatasetRunItemsApi.APIDatasetRunItemsCreateRequest;
import com.langfuse.api.datasets.DatasetsApi.APIDatasetsCreateRequest;
import com.langfuse.api.experiments.ExperimentsApi.APIExperimentsListItemsRequest;
import com.langfuse.api.experiments.ExperimentsApi.APIExperimentsListRequest;
import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.CreateDatasetRunItemRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class ExperimentsApiAsyncTest {

    private static final OffsetDateTime TEST_START = OffsetDateTime.now();
    private static final String DATASET_NAME = "test-async-experiments-dataset-" + UUID.randomUUID();
    private static final String RUN_NAME = "test-async-experiment-run-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static String datasetItemId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void setupDatasetAndExperiment() {
        // Create dataset, item, trace, and run item to populate the experiments endpoint
        client.datasets().datasetsCreate(
                APIDatasetsCreateRequest.newBuilder()
                        .createDatasetRequest(CreateDatasetRequest.builder()
                                .name(DATASET_NAME)
                                .build())
                        .build());

        var item = client.datasetItems().datasetItemsCreate(
                APIDatasetItemsCreateRequest.newBuilder()
                        .createDatasetItemRequest(CreateDatasetItemRequest.builder()
                                .datasetName(DATASET_NAME)
                                .input(Map.of("question", "What is an async experiment?"))
                                .build())
                        .build());

        assertThat(item.getId()).isNotBlank();
        datasetItemId = item.getId();

        // Ingest trace via OTel so the dataset run item can reference it
        OtelTestHelper.ingestTrace(client, TRACE_ID, "async-experiment-test-trace");

        client.datasetRunItems().datasetRunItemsCreate(
                APIDatasetRunItemsCreateRequest.newBuilder()
                        .createDatasetRunItemRequest(CreateDatasetRunItemRequest.builder()
                                .runName(RUN_NAME)
                                .datasetItemId(datasetItemId)
                                .traceId(TRACE_ID)
                                .build())
                        .build());
    }

    @Test
    @Order(2)
    void listExperimentsAsync() {
        assertThat(client.asyncExperiments().experimentsList(
                APIExperimentsListRequest.newBuilder()
                        .fromStartTime(TEST_START.minusMinutes(1))
                        .limit(50)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(response -> {
                    assertThat(response.getData()).isNotNull();
                    assertThat(response.getMeta()).isNotNull();
                });
    }

    @Test
    @Order(2)
    void listExperimentItemsAsync() {
        assertThat(client.asyncExperiments().experimentsListItems(
                APIExperimentsListItemsRequest.newBuilder()
                        .fromStartTime(TEST_START.minusMinutes(1))
                        .limit(50)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(response -> {
                    assertThat(response.getData()).isNotNull();
                    assertThat(response.getMeta()).isNotNull();
                });
    }
}
