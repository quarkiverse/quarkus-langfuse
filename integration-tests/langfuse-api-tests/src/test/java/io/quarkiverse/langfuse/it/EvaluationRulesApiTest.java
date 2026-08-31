package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesDeleteRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesGetRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesListRequest;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Integration tests for the stable Evaluation Rules API.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class EvaluationRulesApiTest {

    private static String evaluationRuleId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void listEvaluationRules() {
        assertThat(client.evaluationRules().evaluationRulesList(
                APIEvaluationRulesListRequest.newBuilder()
                        .build()))
                .satisfies(page -> assertThat(page.getData()).isNotNull());
    }

    @Test
    @Order(2)
    void getUnknownEvaluationRuleFails() {
        assertThatThrownBy(() -> client.evaluationRules().evaluationRulesGet(
                APIEvaluationRulesGetRequest.newBuilder()
                        .evaluationRuleId("does-not-exist-" + UUID.randomUUID())
                        .build()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Order(3)
    @Disabled("Requires an evaluator to be created first, which needs an enterprise plan")
    void evaluationRulesCreate() {
        // Creating an evaluation rule requires evaluator assignments referencing
        // existing evaluators, which are enterprise-only on the self-hosted free tier.
    }

    @Test
    @Order(4)
    @Disabled("Requires evaluation rule creation in a previous step")
    void updateEvaluationRule() {
        // Updating an evaluation rule requires an existing rule ID.
    }

    @Test
    @Order(5)
    @Disabled("Requires evaluation rule creation in a previous step")
    void deleteEvaluationRule() {
        assertThat(client.evaluationRules().evaluationRulesDelete(
                APIEvaluationRulesDeleteRequest.newBuilder()
                        .evaluationRuleId(evaluationRuleId)
                        .build()))
                .satisfies(response -> assertThat(response.getId()).isNotBlank());
    }
}
