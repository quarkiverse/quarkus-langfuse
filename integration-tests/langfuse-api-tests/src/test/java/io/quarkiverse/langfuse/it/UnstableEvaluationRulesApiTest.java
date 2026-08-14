package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesDeleteRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesGetRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesListRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class UnstableEvaluationRulesApiTest {

    private static String evaluationRuleId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void listEvaluationRules() {
        assertThat(client.unstableEvaluationRules().unstableEvaluationRulesList(
                APIUnstableEvaluationRulesListRequest.newBuilder()
                        .build()))
                .satisfies(response -> assertThat(response.getData()).isNotNull());
    }

    @Test
    @Order(2)
    @Disabled("Requires an evaluator to be created first via UnstableEvaluatorsApi")
    void unstableEvaluationRulesCreate() {
        // Creating an evaluation rule requires an evaluator reference (evaluatorId).
        // The create request is a oneOf: unstableCreateLlmAsJudgeEvaluationRuleRequest
        // or unstableCreateCodeEvaluationRuleRequest. Both require an existing evaluator.
    }

    @Test
    @Order(3)
    @Disabled("Requires evaluation rule creation in previous step")
    void unstableEvaluationRulesUpdate() {
        // Updating an evaluation rule requires an existing rule ID.
    }

    @Test
    @Order(3)
    @Disabled("Requires evaluation rule creation in previous step")
    void getEvaluationRule() {
        assertThat(client.unstableEvaluationRules().unstableEvaluationRulesGet(
                APIUnstableEvaluationRulesGetRequest.newBuilder()
                        .evaluationRuleId(evaluationRuleId)
                        .build()))
                .satisfies(rule -> assertThat(rule.getActualInstance()).isNotNull());
    }

    @Test
    @Order(4)
    @Disabled("Requires evaluation rule creation in previous step")
    void deleteEvaluationRule() {
        assertThat(client.unstableEvaluationRules().unstableEvaluationRulesDelete(
                APIUnstableEvaluationRulesDeleteRequest.newBuilder()
                        .evaluationRuleId(evaluationRuleId)
                        .build()))
                .satisfies(response -> assertThat(response.getMessage()).isNotBlank());
    }
}
