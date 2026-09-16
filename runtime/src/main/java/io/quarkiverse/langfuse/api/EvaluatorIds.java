package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CodeEvaluator1;
import com.langfuse.api.model.Evaluator;
import com.langfuse.api.model.LlmAsJudgeEvaluator1;

/**
 * Extracts identifiers from polymorphic evaluator models.
 */
final class EvaluatorIds {

    private EvaluatorIds() {
    }

    /**
     * Extracts the evaluator id from an {@link Evaluator}.
     *
     * @param evaluator the evaluator, may be {@code null}
     * @return the evaluator id, or {@code null} if absent or unrecognized
     */
    static String of(Evaluator evaluator) {
        return Optional.ofNullable(evaluator)
                .map(Evaluator::getActualInstance)
                .flatMap(instance -> Optional.of(instance)
                        .filter(CodeEvaluator1.class::isInstance)
                        .map(CodeEvaluator1.class::cast)
                        .map(CodeEvaluator1::getId)
                        .or(() -> Optional.of(instance)
                                .filter(LlmAsJudgeEvaluator1.class::isInstance)
                                .map(LlmAsJudgeEvaluator1.class::cast)
                                .map(LlmAsJudgeEvaluator1::getId)))
                .orElse(null);
    }
}
