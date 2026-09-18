package io.quarkiverse.langfuse.api;

import com.langfuse.api.evaluators.EvaluatorsApi;
import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsListVersionsRequest;
import com.langfuse.api.model.CursorMeta;
import com.langfuse.api.model.EvaluatorVersion;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.config.LangfuseConfig;

final class DefaultEvaluatorVersionOperations extends AbstractCursorOperations<EvaluatorVersion>
        implements EvaluatorVersionOperations {

    // The parent evaluator id is captured in the fetcher lambda handed to the engine, which is why a
    // parent-scoped collection needs neither a widened CursorFetcher nor a change to Pagination: this
    // view is just the same class constructed over a different fetcher. The id is already validated by
    // the accessor that builds this view.
    DefaultEvaluatorVersionOperations(EvaluatorsApi evaluatorsApi, LangfuseConfig config, String evaluatorId) {
        super(cursor -> fetch(evaluatorsApi, evaluatorId, cursor), config);
    }

    private static CursorResult<EvaluatorVersion> fetch(EvaluatorsApi evaluatorsApi, String evaluatorId, Cursor cursor) {
        var response = evaluatorsApi.evaluatorsListVersions(APIEvaluatorsListVersionsRequest.newBuilder()
                .evaluatorId(evaluatorId)
                .limit(cursor.limit())
                .cursor(cursor.value().orElse(null))
                .build());

        return CursorResults.from(cursor, response.getData(), response.getMeta(), CursorMeta::getCursor);
    }
}
