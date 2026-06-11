package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.datasetItems.DatasetItemsApi;
import com.langfuse.api.datasets.DatasetsApi;
import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.DatasetItem;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Integration tests for the Dataset Items API.
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class DatasetItemsApiTest {

    @Inject
    LangfuseApi client;

    private static final String DATASET_NAME = "test-dataset-items-" + UUID.randomUUID();
    private static String datasetItemId;

    @Test
    @Order(1)
    void createDatasetAndItem() {
        var dataset = client.datasets().datasetsCreate(
                DatasetsApi.APIDatasetsCreateRequest.newBuilder()
                        .createDatasetRequest(CreateDatasetRequest.builder()
                                .name(DATASET_NAME)
                                .build())
                        .build());

        assertThat(dataset.getName()).isEqualTo(DATASET_NAME);

        assertThat(client.datasetItems().datasetItemsCreate(
                DatasetItemsApi.APIDatasetItemsCreateRequest.newBuilder()
                        .createDatasetItemRequest(CreateDatasetItemRequest.builder()
                                .datasetName(DATASET_NAME)
                                .input(Map.of("question", "What is Java?"))
                                .expectedOutput(Map.of("answer", "A programming language"))
                                .build())
                        .build()))
                .satisfies(item -> {
                    assertThat(item.getId()).isNotBlank();
                    assertThat(item.getDatasetName()).isEqualTo(DATASET_NAME);
                    assertThat(item.getCreatedAt()).isNotNull();
                    datasetItemId = item.getId();
                });
    }

    @Test
    @Order(2)
    void getDatasetItem() {
        assertThat(client.datasetItems().datasetItemsGet(
                DatasetItemsApi.APIDatasetItemsGetRequest.newBuilder()
                        .id(datasetItemId)
                        .build()))
                .satisfies(item -> {
                    assertThat(item.getId()).isEqualTo(datasetItemId);
                    assertThat(item.getDatasetName()).isEqualTo(DATASET_NAME);
                    assertThat(item.getInput()).isNotNull();
                    assertThat(item.getExpectedOutput()).isNotNull();
                    assertThat(item.getCreatedAt()).isNotNull();
                });
    }

    @Test
    @Order(2)
    void listDatasetItems() {
        var items = client.datasetItems().datasetItemsList(
                DatasetItemsApi.APIDatasetItemsListRequest.newBuilder()
                        .datasetName(DATASET_NAME)
                        .build());

        assertThat(items.getData())
                .hasSize(1)
                .first()
                .extracting(DatasetItem::getId, DatasetItem::getDatasetName)
                .containsExactly(datasetItemId, DATASET_NAME);

        assertThat(items.getMeta().getTotalItems())
                .isEqualTo(1);
    }
}
