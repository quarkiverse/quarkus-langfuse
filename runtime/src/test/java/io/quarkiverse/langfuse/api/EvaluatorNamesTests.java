package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

import com.langfuse.api.model.CodeEvaluator1;
import com.langfuse.api.model.CodeEvaluatorSourceCodeLanguage;
import com.langfuse.api.model.CreateCodeEvaluatorRequest1;
import com.langfuse.api.model.CreateEvaluatorRequest;
import com.langfuse.api.model.CreateLlmAsJudgeEvaluatorRequest1;
import com.langfuse.api.model.Evaluator;
import com.langfuse.api.model.LlmAsJudgeEvaluator1;

import tools.jackson.databind.json.JsonMapper;

class EvaluatorNamesTests {

    @Test
    void extractsNameFromCodeEvaluator() {
        var code = CodeEvaluator1.builder()
                .name("my-code-evaluator")
                .sourceCode("return 1")
                .sourceCodeLanguage(CodeEvaluatorSourceCodeLanguage.PYTHON)
                .type(CodeEvaluator1.TypeEnum.CODE)
                .build();
        var evaluator = new Evaluator(code);

        assertThat(EvaluatorNames.of(evaluator)).isEqualTo("my-code-evaluator");
    }

    @Test
    void extractsNameFromLlmAsJudgeEvaluator() {
        var llm = LlmAsJudgeEvaluator1.builder()
                .name("my-llm-evaluator")
                .type(LlmAsJudgeEvaluator1.TypeEnum.LLM_AS_JUDGE)
                .build();
        var evaluator = new Evaluator(llm);

        assertThat(EvaluatorNames.of(evaluator)).isEqualTo("my-llm-evaluator");
    }

    @Test
    void returnsNullWhenEvaluatorOrInstanceIsNull() {
        assertThat(EvaluatorNames.of((Evaluator) null)).isNull();
        assertThat(EvaluatorNames.of(new Evaluator())).isNull();
    }

    @Test
    void extractsNameFromCreateCodeEvaluatorRequest() {
        var code = CreateCodeEvaluatorRequest1.builder()
                .name("my-create-code-evaluator")
                .sourceCode("return 1")
                .sourceCodeLanguage(CodeEvaluatorSourceCodeLanguage.PYTHON)
                .type(CreateCodeEvaluatorRequest1.TypeEnum.CODE)
                .build();
        var request = new CreateEvaluatorRequest(code);

        assertThat(EvaluatorNames.of(request)).isEqualTo("my-create-code-evaluator");
    }

    @Test
    void extractsNameFromCreateLlmAsJudgeEvaluatorRequest() {
        var llm = CreateLlmAsJudgeEvaluatorRequest1.builder()
                .name("my-create-llm-evaluator")
                .type(CreateLlmAsJudgeEvaluatorRequest1.TypeEnum.LLM_AS_JUDGE)
                .build();
        var request = new CreateEvaluatorRequest(llm);

        assertThat(EvaluatorNames.of(request)).isEqualTo("my-create-llm-evaluator");
    }

    @Test
    void throwsOnNullCreateRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> EvaluatorNames.of((CreateEvaluatorRequest) null))
                .withMessage("request must not be null");
    }

    @Test
    void testJsonDeserialization() throws Exception {
        var json = """
                {
                  "id": "evaluator-1",
                  "name": "evaluator-1",
                  "type": "code",
                  "sourceCode": "return 1",
                  "sourceCodeLanguage": "PYTHON"
                }
                """;
        var mapper = new JsonMapper();
        var evaluator = mapper.readValue(json, Evaluator.class);

        assertThat(EvaluatorNames.of(evaluator)).isEqualTo("evaluator-1");
    }
}
