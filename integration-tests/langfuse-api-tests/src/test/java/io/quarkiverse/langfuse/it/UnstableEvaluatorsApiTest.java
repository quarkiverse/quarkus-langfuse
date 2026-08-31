package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.UnstableCodeEvaluatorSourceCodeLanguage;
import com.langfuse.api.model.UnstableCreateCodeEvaluatorRequest;
import com.langfuse.api.model.UnstableCreateEvaluatorRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsCreateRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsListRequest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class UnstableEvaluatorsApiTest {

    @Inject
    LangfuseApi client;

    @Test
    void listEvaluators() {
        // List endpoint works even when no project-owned evaluators exist
        assertThat(client.unstableEvaluators().unstableEvaluatorsList(
                APIUnstableEvaluatorsListRequest.newBuilder()
                        .build()))
                .satisfies(list -> assertThat(list.getData()).isNotNull());
    }

    @Test
    void createCodeEvaluatorRequiresEnterprisePlan() {
        // Code evaluator creation returns 403 on self-hosted free tier
        var createRequest = new UnstableCreateEvaluatorRequest(
                UnstableCreateCodeEvaluatorRequest.builder()
                        .name("test-evaluator-" + UUID.randomUUID().toString().substring(0, 8))
                        .sourceCode("def evaluate(output, expected_output, input, metadata):\n  return 1.0")
                        .sourceCodeLanguage(UnstableCodeEvaluatorSourceCodeLanguage.PYTHON)
                        .type(UnstableCreateCodeEvaluatorRequest.TypeEnum.CODE)
                        .build());

        assertThatThrownBy(() -> client.unstableEvaluators().unstableEvaluatorsCreate(
                APIUnstableEvaluatorsCreateRequest.newBuilder()
                        .unstableCreateEvaluatorRequest(createRequest)
                        .build()))
                .isInstanceOf(RuntimeException.class);
    }
}
