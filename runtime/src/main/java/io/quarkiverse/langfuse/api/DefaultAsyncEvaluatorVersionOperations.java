package io.quarkiverse.langfuse.api;

import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsListVersionsRequest;
import com.langfuse.api.evaluators.async.EvaluatorsApi;
import com.langfuse.api.model.CursorMeta;
import com.langfuse.api.model.EvaluatorVersion;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncEvaluatorVersionOperations extends AbstractAsyncCursorOperations<EvaluatorVersion>
        implements AsyncEvaluatorVersionOperations {

    // The parent evaluator id is captured in the fetcher lambda handed to the engine, so neither
    // AsyncCursorFetcher nor AsyncPagination needs widening. The id is already validated by the
    // accessor that builds this view, outside any deferred supplier.
    DefaultAsyncEvaluatorVersionOperations(EvaluatorsApi evaluatorsApi, LangfuseConfig config, String evaluatorId) {
        super(cursor -> fetch(evaluatorsApi, evaluatorId, cursor), config);
    }

    private static Uni<CursorResult<EvaluatorVersion>> fetch(EvaluatorsApi evaluatorsApi, String evaluatorId,
            Cursor cursor) {
        return Uni.createFrom()
                .completionStage(() -> evaluatorsApi.evaluatorsListVersions(APIEvaluatorsListVersionsRequest.newBuilder()
                        .evaluatorId(evaluatorId)
                        .limit(cursor.limit())
                        .cursor(cursor.value().orElse(null))
                        .build()))
                .map(response -> CursorResults.from(cursor, response.getData(), response.getMeta(),
                        CursorMeta::getCursor));
    }
}
