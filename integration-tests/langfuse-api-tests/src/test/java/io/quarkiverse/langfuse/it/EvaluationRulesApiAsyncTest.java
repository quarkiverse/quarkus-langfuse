package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesGetRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesListRequest;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Async integration tests for the stable Evaluation Rules API.
 */
@QuarkusTest
class EvaluationRulesApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Test
    void listEvaluationRulesAsync() {
        assertThat(client.asyncEvaluationRules().evaluationRulesList(
                APIEvaluationRulesListRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(page -> assertThat(page.getData()).isNotNull());
    }

    @Test
    void getUnknownEvaluationRuleFailsAsync() {
        assertThat(client.asyncEvaluationRules().evaluationRulesGet(
                APIEvaluationRulesGetRequest.newBuilder()
                        .evaluationRuleId("does-not-exist-" + UUID.randomUUID())
                        .build()))
                .failsWithin(Duration.ofSeconds(5));
    }
}
