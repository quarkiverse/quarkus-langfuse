package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsCreateRequest;
import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsListRequest;
import com.langfuse.api.model.CodeEvaluatorSourceCodeLanguage;
import com.langfuse.api.model.CreateCodeEvaluatorRequest1;
import com.langfuse.api.model.CreateEvaluatorRequest;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Async integration tests for the stable Evaluators API.
 */
@QuarkusTest
class EvaluatorsApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Test
    void listEvaluatorsAsync() {
        assertThat(client.asyncEvaluators().evaluatorsList(
                APIEvaluatorsListRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(page -> assertThat(page.getData()).isNotNull());
    }

    @Test
    void createCodeEvaluatorRequiresEnterprisePlan() {
        // Code evaluator creation returns 403 on the self-hosted free tier.
        var createRequest = new CreateEvaluatorRequest(
                CreateCodeEvaluatorRequest1.builder()
                        .name("async-test-evaluator-" + UUID.randomUUID().toString().substring(0, 8))
                        .sourceCode("def evaluate(output, expected_output, input, metadata):\n  return 1.0")
                        .sourceCodeLanguage(CodeEvaluatorSourceCodeLanguage.PYTHON)
                        .type(CreateCodeEvaluatorRequest1.TypeEnum.CODE)
                        .build());

        assertThat(client.asyncEvaluators().evaluatorsCreate(
                APIEvaluatorsCreateRequest.newBuilder()
                        .createEvaluatorRequest(createRequest)
                        .build()))
                .failsWithin(Duration.ofSeconds(5));
    }
}
