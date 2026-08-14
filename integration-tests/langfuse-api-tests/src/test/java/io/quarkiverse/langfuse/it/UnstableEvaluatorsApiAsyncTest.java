package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.UnstableCodeEvaluatorSourceCodeLanguage;
import com.langfuse.api.model.UnstableCreateEvaluatorRequest;
import com.langfuse.api.model.UnstableCreateEvaluatorRequestOneOf1;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsCreateRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsListRequest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class UnstableEvaluatorsApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Test
    void listEvaluatorsAsync() {
        assertThat(client.asyncUnstableEvaluators().unstableEvaluatorsList(
                APIUnstableEvaluatorsListRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(list -> assertThat(list.getData()).isNotNull());
    }

    @Test
    void createCodeEvaluatorRequiresEnterprisePlan() {
        // Code evaluator creation returns 403 on self-hosted free tier
        var createRequest = new UnstableCreateEvaluatorRequest(
                UnstableCreateEvaluatorRequestOneOf1.builder()
                        .name("async-test-evaluator-" + UUID.randomUUID().toString().substring(0, 8))
                        .sourceCode("def evaluate(output, expected_output, input, metadata):\n  return 1.0")
                        .sourceCodeLanguage(UnstableCodeEvaluatorSourceCodeLanguage.PYTHON)
                        .type(UnstableCreateEvaluatorRequestOneOf1.TypeEnum.CODE)
                        .build());

        assertThat(client.asyncUnstableEvaluators().unstableEvaluatorsCreate(
                APIUnstableEvaluatorsCreateRequest.newBuilder()
                        .unstableCreateEvaluatorRequest(createRequest)
                        .build()))
                .failsWithin(Duration.ofSeconds(5));
    }
}
