package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoresResponse;
import com.langfuse.api.model.GetScoresV3Meta;
import com.langfuse.api.model.ScoreV3;
import com.langfuse.api.scores.ScoresApi;
import com.langfuse.api.scores.ScoresApi.APIScoresCreateRequest;
import com.langfuse.api.scores.ScoresApi.APIScoresDeleteRequest;
import com.langfuse.api.scores.ScoresApi.APIScoresGetManyRequest;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

// A single generated API backs all three operations, even though Langfuse does not version them
// alike: the listing is v3, the create and the delete are unversioned.
final class DefaultScoreOperations extends AbstractCursorOperations<ScoreV3> implements ScoreOperations {
    private final ScoresApi scoresApi;
    private final LangfuseConfig config;

    DefaultScoreOperations(ScoresApi scoresApi, LangfuseConfig config) {
        this(scoresApi, config, ScoreFilter.none());
    }

    // The filter is captured in the fetcher lambda handed to the engine, which is why a filtered view
    // needs neither a widened CursorFetcher nor a change to Pagination: a view is just this class
    // constructed over a different fetcher.
    private DefaultScoreOperations(ScoresApi scoresApi, LangfuseConfig config, ScoreFilter filter) {
        super(cursor -> fetch(scoresApi, cursor, filter), config);
        this.scoresApi = scoresApi;
        this.config = config;
    }

    @Override
    public ScoreOperations matching(ScoreFilter filter) {
        return new DefaultScoreOperations(this.scoresApi, this.config,
                ValidationUtils.ensureNotNull(filter, "Filter"));
    }

    @Override
    public CreateScoresResponse create(Collection<CreateScoreRequest> requests) {
        ValidationUtils.ensureNotNull(requests, "requests");

        return this.scoresApi.scoresCreate(APIScoresCreateRequest.newBuilder()
                .createScoreRequest(scores(requests))
                .build());
    }

    @Override
    public DeletionResult deleteById(Collection<String> ids) {
        // Optional::of is the identity resolver: the delete endpoint takes the score id directly, so
        // nothing has to be resolved and no listing request is issued.
        return Deletions.deleteAll(ids, "Score id", Optional::of, this::delete, deleteConcurrency());
    }

    // The generated builder keeps the list it is handed, so a defensive copy is taken here; the same
    // pass rejects null elements, which the generated layer would otherwise serialise as JSON nulls.
    private static List<CreateScoreRequest> scores(Collection<CreateScoreRequest> requests) {
        return requests.stream()
                .map(request -> ValidationUtils.ensureNotNull(request, "request"))
                .toList();
    }

    private void delete(String id) {
        this.scoresApi.scoresDelete(APIScoresDeleteRequest.newBuilder()
                .scoreId(id)
                .build());
    }

    private static CursorResult<ScoreV3> fetch(ScoresApi scoresApi, Cursor cursor, ScoreFilter filter) {
        var response = scoresApi.scoresGetMany(request(cursor, filter));

        return CursorResults.from(cursor, response.getData(), response.getMeta(), GetScoresV3Meta::getCursor);
    }

    // The generated builder types source, dataType and the field groups as plain Strings, so the typed
    // values this layer exposes are mapped here at the boundary rather than widening the public surface
    // to match the generated one.
    //
    // Langfuse caps limit at 100 and answers HTTP 400 above it. The cursor's limit comes from
    // quarkus.langfuse.api.default-batch-size, which defaults to 50; a caller raising it past 100 gets
    // that 400 rather than a silently clamped request.
    private static APIScoresGetManyRequest request(Cursor cursor, ScoreFilter filter) {
        var builder = APIScoresGetManyRequest.newBuilder()
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
