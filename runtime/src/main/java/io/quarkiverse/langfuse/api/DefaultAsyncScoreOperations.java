package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.langfuse.api.legacyScoreV1.LegacyScoreV1Api.APILegacyScoreV1DeleteRequest;
import com.langfuse.api.legacyScoreV1.async.LegacyScoreV1Api;
import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreResponse;
import com.langfuse.api.model.GetScoresV3Meta;
import com.langfuse.api.model.ScoreV3;
import com.langfuse.api.scores.ScoresApi.APIScoresCreateRequest;
import com.langfuse.api.scores.async.ScoresApi;
import com.langfuse.api.scoresV3.ScoresV3Api.APIScoresV3GetManyV3Request;
import com.langfuse.api.scoresV3.async.ScoresV3Api;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

// Three generated APIs rather than the usual one: Langfuse splits scores across the v3 listing, the
// unversioned create and the score delete, and none of the three carries the other two's operations.
// Each API class shares its simple name with its synchronous twin, so the asynchronous interfaces are
// imported here while the request builders keep coming from the synchronous packages.
final class DefaultAsyncScoreOperations extends AbstractAsyncCursorOperations<ScoreV3>
        implements AsyncScoreOperations {
    private final ScoresV3Api scoresV3Api;
    private final ScoresApi scoresApi;
    private final LegacyScoreV1Api legacyScoreV1Api;
    private final LangfuseConfig config;

    DefaultAsyncScoreOperations(ScoresV3Api scoresV3Api, ScoresApi scoresApi, LegacyScoreV1Api legacyScoreV1Api,
            LangfuseConfig config) {
        this(scoresV3Api, scoresApi, legacyScoreV1Api, config, ScoreFilter.none());
    }

    // The filter is captured in the fetcher lambda handed to the engine, which is why a filtered view
    // needs neither a widened AsyncCursorFetcher nor a change to AsyncPagination: a view is just this
    // class constructed over a different fetcher.
    private DefaultAsyncScoreOperations(ScoresV3Api scoresV3Api, ScoresApi scoresApi,
            LegacyScoreV1Api legacyScoreV1Api, LangfuseConfig config, ScoreFilter filter) {
        super(cursor -> fetch(scoresV3Api, cursor, filter), config);
        this.scoresV3Api = scoresV3Api;
        this.scoresApi = scoresApi;
        this.legacyScoreV1Api = legacyScoreV1Api;
        this.config = config;
    }

    @Override
    public AsyncScoreOperations matching(ScoreFilter filter) {
        return new DefaultAsyncScoreOperations(this.scoresV3Api, this.scoresApi, this.legacyScoreV1Api, this.config,
                ValidationUtils.ensureNotNull(filter, "Filter"));
    }

    @Override
    public Uni<CreateScoreResponse> create(CreateScoreRequest request) {
        // Validated here rather than inside the completionStage supplier: a throw in there becomes a
        // failure event, and a null request must surface as a thrown IllegalArgumentException.
        ValidationUtils.ensureNotNull(request, "request");

        return Uni.createFrom()
                .completionStage(() -> this.scoresApi.scoresCreate(APIScoresCreateRequest.newBuilder()
                        .createScoreRequest(request)
                        .build()));
    }

    @Override
    public Uni<DeletionResult> deleteById(Collection<String> ids) {
        // Validation runs before the deferred wrapper so malformed input throws from the call, as the
        // rest of this tree does. Inside the supplier it would surface as a failed Uni at subscription
        // instead. deleteConcurrency() stays inside, so the config is read per subscription.
        DeletionIdentifiers.validated(ids, "Score id");

        // Uni.createFrom()::item is the identity resolver: the delete endpoint takes the score id
        // directly, so nothing has to be resolved and no listing request is issued.
        return Uni.createFrom()
                .deferred(() -> AsyncDeletions.deleteAll(ids, "Score id", Uni.createFrom()::item, this::delete,
                        deleteConcurrency()));
    }

    private Uni<?> delete(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.legacyScoreV1Api.legacyScoreV1Delete(APILegacyScoreV1DeleteRequest
                        .newBuilder()
                        .scoreId(id)
                        .build()));
    }

    private static Uni<CursorResult<ScoreV3>> fetch(ScoresV3Api scoresV3Api, Cursor cursor, ScoreFilter filter) {
        var request = request(cursor, filter);

        return Uni.createFrom()
                .completionStage(() -> scoresV3Api.scoresV3GetManyV3(request))
                .map(response -> CursorResults.from(cursor, response.getData(), response.getMeta(),
                        GetScoresV3Meta::getCursor));
    }

    // The generated builder types source, dataType and the field groups as plain Strings, so the typed
    // values this layer exposes are mapped here at the boundary rather than widening the public surface
    // to match the generated one.
    //
    // Langfuse caps limit at 100 and answers HTTP 400 above it. The cursor's limit comes from
    // quarkus.langfuse.api.default-batch-size, which defaults to 50; a caller raising it past 100 gets
    // that 400 rather than a silently clamped request.
    private static APIScoresV3GetManyV3Request request(Cursor cursor, ScoreFilter filter) {
        var builder = APIScoresV3GetManyV3Request.newBuilder()
                .limit(cursor.limit())
                .cursor(cursor.value().orElse(null));

        fields(filter.fields()).ifPresent(builder::fields);
        filter.id().ifPresent(builder::id);
        filter.name().ifPresent(builder::name);
        filter.source().map(Enum::name).ifPresent(builder::source);
        filter.dataType().map(Enum::name).ifPresent(builder::dataType);
        filter.environment().ifPresent(builder::environment);
        filter.configId().ifPresent(builder::configId);
        filter.queueId().ifPresent(builder::queueId);
        filter.authorUserId().ifPresent(builder::authorUserId);
        filter.value().ifPresent(builder::value);
        filter.valueMin().ifPresent(builder::valueMin);
        filter.valueMax().ifPresent(builder::valueMax);
        filter.traceId().ifPresent(builder::traceId);
        filter.sessionId().ifPresent(builder::sessionId);
        filter.observationId().ifPresent(builder::observationId);
        filter.experimentId().ifPresent(builder::experimentId);
        filter.fromTimestamp().ifPresent(builder::fromTimestamp);
        filter.toTimestamp().ifPresent(builder::toTimestamp);

        return builder.build();
    }

    // Langfuse names the groups in lower case and rejects anything else with HTTP 400, so the enum
    // constants are lowered here. Sorted so the same set always renders the same parameter, which
    // keeps the request stable for caches and for tests.
    private static Optional<String> fields(Set<ScoreFieldGroup> fields) {
        return Optional.of(fields)
                .filter(groups -> !groups.isEmpty())
                .map(groups -> groups.stream()
                        .map(group -> group.name().toLowerCase(Locale.ROOT))
                        .sorted()
                        .collect(Collectors.joining(",")));
    }
}
