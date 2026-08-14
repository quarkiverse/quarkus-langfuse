package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

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
class LlmConnectionsApiAsyncTest {

    private static String connectionId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void upsertLlmConnection() {
        // Create an LLM connection with a dummy key — the key doesn't need to be valid
        assertThat(client.asyncLlmConnections().llmConnectionsUpsert(
                APILlmConnectionsUpsertRequest.newBuilder()
                        .upsertLlmConnectionRequest(UpsertLlmConnectionRequest.builder()
                                .provider("async-test-provider")
                                .adapter(LlmAdapter.ANTHROPIC)
                                .secretKey("sk-ant-test-dummy-key-for-async-tests")
                                .withDefaultModels(true)
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(connection -> {
                    assertThat(connection.getId()).isNotBlank();
                    assertThat(connection.getProvider()).isEqualTo("async-test-provider");
                    assertThat(connection.getAdapter()).isEqualTo("anthropic");
                    connectionId = connection.getId();
                });
    }

    @Test
    @Order(2)
    void listLlmConnectionsContainsCreated() {
        assertThat(client.asyncLlmConnections().llmConnectionsList(
                APILlmConnectionsListRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(list -> assertThat(list.getData())
                        .isNotEmpty()
                        .anyMatch(c -> connectionId.equals(c.getId())));
    }

    @Test
    @Order(3)
    void deleteLlmConnection() {
        assertThat(client.asyncLlmConnections().llmConnectionsDelete(
                APILlmConnectionsDeleteRequest.newBuilder()
                        .id(connectionId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }
}
