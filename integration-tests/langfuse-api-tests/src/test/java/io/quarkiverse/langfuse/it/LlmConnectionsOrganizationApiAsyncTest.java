package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsDeleteOrganizationRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsListOrganizationRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsUpsertOrganizationRequest;
import com.langfuse.api.model.LlmAdapter;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkus.test.junit.QuarkusTest;

@Disabled("Requires an organization-scoped API key")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class LlmConnectionsOrganizationApiAsyncTest {

    private static String connectionId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void upsertLlmConnection() {
        // Create an LLM connection with a dummy key — the key doesn't need to be valid
        assertThat(client.asyncLlmConnections().llmConnectionsUpsertOrganization(
                APILlmConnectionsUpsertOrganizationRequest.newBuilder()
                        .upsertLlmConnectionRequest(UpsertLlmConnectionRequest.builder()
                                .provider("async-test-org-provider")
                                .adapter(LlmAdapter.ANTHROPIC)
                                .secretKey("sk-ant-test-dummy-key-for-async-tests")
                                .withDefaultModels(true)
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(connection -> {
                    assertThat(connection.getId()).isNotBlank();
                    assertThat(connection.getProvider()).isEqualTo("async-test-org-provider");
                    assertThat(connection.getAdapter()).isEqualTo("anthropic");
                    connectionId = connection.getId();
                });
    }

    @Test
    @Order(2)
    void listLlmConnectionsContainsCreated() {
        assertThat(client.asyncLlmConnections().llmConnectionsListOrganization(
                APILlmConnectionsListOrganizationRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(list -> assertThat(list.getData())
                        .isNotEmpty()
                        .anyMatch(c -> connectionId.equals(c.getId())));
    }

    @Test
    @Order(3)
    void deleteLlmConnection() {
        assertThat(client.asyncLlmConnections().llmConnectionsDeleteOrganization(
                APILlmConnectionsDeleteOrganizationRequest.newBuilder()
                        .id(connectionId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }
}
