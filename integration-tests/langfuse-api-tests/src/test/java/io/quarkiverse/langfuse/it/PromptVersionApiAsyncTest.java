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
import com.langfuse.api.model.CreatePromptRequest;
import com.langfuse.api.model.CreateTextPromptRequest;
import com.langfuse.api.model.CreateTextPromptType;
import com.langfuse.api.model.PromptVersionUpdateRequest;
import com.langfuse.api.promptVersion.PromptVersionApi.APIPromptVersionUpdateRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsCreateRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsGetRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class PromptVersionApiAsyncTest {

    private static final String PROMPT_NAME = "async-test-prompt-version-" + UUID.randomUUID();

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void createPrompt() {
        client.prompts().promptsCreate(
                APIPromptsCreateRequest.newBuilder()
                        .createPromptRequest(new CreatePromptRequest(
                                CreateTextPromptRequest.builder()
                                        .name(PROMPT_NAME)
                                        .prompt("Async Version 1: Hello {{name}}")
                                        .type(CreateTextPromptType.TEXT)
                                        .labels(List.of("staging"))
                                        .build()))
                        .build());
    }

    @Test
    @Order(2)
    void updatePromptVersionAsync() {
        assertThat(client.asyncPromptVersion().promptVersionUpdate(
                APIPromptVersionUpdateRequest.newBuilder()
                        .name(PROMPT_NAME)
                        .version(1)
                        .promptVersionUpdateRequest(PromptVersionUpdateRequest.builder()
                                .newLabels(List.of("production"))
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }

    @Test
    @Order(3)
    void fetchUpdatedPromptVersionAsync() {
        assertThat(client.asyncPrompts().promptsGet(
                APIPromptsGetRequest.newBuilder()
                        .promptName(PROMPT_NAME)
                        .label("production")
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }
}
