package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsDeleteRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsListRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsUpsertRequest;
import com.langfuse.api.model.LlmAdapter;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class LlmConnectionsApiTest {

    private static String connectionId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void upsertLlmConnection() {
        // Create an LLM connection with a dummy key — the key doesn't need to be valid
        assertThat(client.llmConnections().llmConnectionsUpsert(
                APILlmConnectionsUpsertRequest.newBuilder()
                        .upsertLlmConnectionRequest(UpsertLlmConnectionRequest.builder()
                                .provider("test-provider")
                                .adapter(LlmAdapter.OPENAI)
                                .secretKey("sk-test-dummy-key-for-integration-tests")
                                .withDefaultModels(true)
                                .build())
                        .build()))
                .satisfies(connection -> {
                    assertThat(connection.getId()).isNotBlank();
                    assertThat(connection.getProvider()).isEqualTo("test-provider");
                    assertThat(connection.getAdapter()).isEqualTo("openai");
                    // Secret key should be masked in the response
                    assertThat(connection.getDisplaySecretKey()).isNotBlank();
                    connectionId = connection.getId();
                });
    }

    @Test
    @Order(2)
    void listLlmConnectionsContainsCreated() {
        assertThat(client.llmConnections().llmConnectionsList(
                APILlmConnectionsListRequest.newBuilder()
                        .build()))
                .satisfies(list -> assertThat(list.getData())
                        .isNotEmpty()
                        .anyMatch(c -> connectionId.equals(c.getId())));
    }

    @Test
    @Order(3)
    void deleteLlmConnection() {
        assertThat(client.llmConnections().llmConnectionsDelete(
                APILlmConnectionsDeleteRequest.newBuilder()
                        .id(connectionId)
                        .build()))
                .isNotNull();
    }
}
