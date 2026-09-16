package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langfuse.api.model.CodeEvaluator1;
import com.langfuse.api.model.CodeEvaluatorSourceCodeLanguage;
import com.langfuse.api.model.Evaluator;
import com.langfuse.api.model.LlmAsJudgeEvaluator1;

class EvaluatorIdsTests {

    @Test
    void extractsIdFromCodeEvaluator() {
        var code = CodeEvaluator1.builder()
                .id("code-evaluator-1")
                .name("my-code-evaluator")
                .sourceCode("return 1")
                .sourceCodeLanguage(CodeEvaluatorSourceCodeLanguage.PYTHON)
                .type(CodeEvaluator1.TypeEnum.CODE)
                .build();
        var evaluator = new Evaluator(code);

        assertThat(EvaluatorIds.of(evaluator)).isEqualTo("code-evaluator-1");
    }

    @Test
    void extractsIdFromLlmAsJudgeEvaluator() {
        var llm = LlmAsJudgeEvaluator1.builder()
                .id("llm-evaluator-1")
                .name("my-llm-evaluator")
                .type(LlmAsJudgeEvaluator1.TypeEnum.LLM_AS_JUDGE)
                .build();
        var evaluator = new Evaluator(llm);

        assertThat(EvaluatorIds.of(evaluator)).isEqualTo("llm-evaluator-1");
    }

    @Test
    void returnsNullWhenEvaluatorOrInstanceIsNull() {
        assertThat(EvaluatorIds.of(null)).isNull();
        assertThat(EvaluatorIds.of(new Evaluator())).isNull();
    }

    @Test
    void returnsNullWhenTheIdIsAbsent() {
        // An evaluator whose id cannot be extracted must not blow up here: delete-by-name turns a null id
        // into a NotFound rather than issuing a request with a null path parameter.
        var llm = LlmAsJudgeEvaluator1.builder()
                .name("my-llm-evaluator")
                .type(LlmAsJudgeEvaluator1.TypeEnum.LLM_AS_JUDGE)
                .build();

        assertThat(EvaluatorIds.of(new Evaluator(llm))).isNull();
    }

    @Test
    void extractsIdFromDeserializedEvaluator() throws Exception {
        var json = """
                {
                  "id": "evaluator-1",
                  "name": "evaluator-1",
                  "type": "code",
                  "sourceCode": "return 1",
                  "sourceCodeLanguage": "PYTHON"
                }
                """;
        var mapper = new ObjectMapper();
        var evaluator = mapper.readValue(json, Evaluator.class);

        assertThat(EvaluatorIds.of(evaluator)).isEqualTo("evaluator-1");
    }
}
