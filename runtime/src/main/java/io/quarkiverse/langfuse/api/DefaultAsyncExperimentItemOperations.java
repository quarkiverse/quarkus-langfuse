package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.langfuse.api.experiments.ExperimentsApi.APIExperimentsListItemsRequest;
import com.langfuse.api.experiments.async.ExperimentsApi;
import com.langfuse.api.model.ExperimentItem;
import com.langfuse.api.model.ExperimentsResponseMeta;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncExperimentItemOperations extends AbstractAsyncCursorOperations<ExperimentItem>
        implements AsyncExperimentItemOperations {
    private final ExperimentsApi experimentsApi;
    private final LangfuseConfig config;
    private final OffsetDateTime fromStartTime;
    private final OffsetDateTime toStartTime;

    // Both bounds are already validated by the gateway that constructs this, which is the only way to
    // reach the domain at all.
    DefaultAsyncExperimentItemOperations(ExperimentsApi experimentsApi, LangfuseConfig config,
            OffsetDateTime fromStartTime, OffsetDateTime toStartTime) {
        this(experimentsApi, config, fromStartTime, toStartTime, ExperimentItemFilter.none());
    }

    // The time window and the filter are captured in the fetcher lambda handed to the engine, which is
    // why neither needs a widened AsyncCursorFetcher nor a change to AsyncPagination: a view is just
    // this class constructed over a different fetcher.
    private DefaultAsyncExperimentItemOperations(ExperimentsApi experimentsApi, LangfuseConfig config,
            OffsetDateTime fromStartTime, OffsetDateTime toStartTime, ExperimentItemFilter filter) {
        super(cursor -> fetch(experimentsApi, cursor, fromStartTime, toStartTime, filter), config);
        this.experimentsApi = experimentsApi;
        this.config = config;
        this.fromStartTime = fromStartTime;
        this.toStartTime = toStartTime;
    }

    @Override
    public AsyncExperimentItemOperations matching(ExperimentItemFilter filter) {
        return new DefaultAsyncExperimentItemOperations(this.experimentsApi, this.config, this.fromStartTime,
                this.toStartTime, ValidationUtils.ensureNotNull(filter, "Filter"));
    }

    private static Uni<CursorResult<ExperimentItem>> fetch(ExperimentsApi experimentsApi, Cursor cursor,
            OffsetDateTime fromStartTime, OffsetDateTime toStartTime, ExperimentItemFilter filter) {
        // Built outside the supplier: assembling the request reads nothing that has to be deferred, and
        // the supplier overload is used purely so the call itself starts at subscription rather than
        // assembly time.
        var request = request(cursor, fromStartTime, toStartTime, filter);

        return Uni.createFrom()
                .completionStage(() -> experimentsApi.experimentsListItems(request))
                .map(response -> CursorResults.from(cursor, response.getData(), response.getMeta(),
                        ExperimentsResponseMeta::getCursor));
    }

    // limit and cursor come from the traversal rather than from the filter, which is why
    // ExperimentItemFilter does not carry them; the two time bounds come from the gateway for the same
    // reason. The field groups are typed here and rendered to the comma-separated string the generated
    // builder wants at this boundary.
    private static APIExperimentsListItemsRequest request(Cursor cursor, OffsetDateTime fromStartTime,
            OffsetDateTime toStartTime, ExperimentItemFilter filter) {
        var builder = APIExperimentsListItemsRequest.newBuilder()
                .limit(cursor.limit())
                .cursor(cursor.value().orElse(null))
                .fromStartTime(fromStartTime)
                .toStartTime(toStartTime);

        fields(filter.fields()).ifPresent(builder::fields);
        filter.scoreLimit().ifPresent(builder::scoreLimit);
        filter.experimentId().ifPresent(builder::experimentId);
        filter.experimentName().ifPresent(builder::experimentName);
        filter.experimentItemId().ifPresent(builder::experimentItemId);
        filter.datasetId().ifPresent(builder::datasetId);
        filter.filter().ifPresent(builder::filter);

        return builder.build();
    }

    // Sorted so the same set always renders the same parameter, which keeps the request stable for
    // caches and for tests. Sorted case-insensitively on the wire spelling, so the camel-cased groups
    // interleave with the lower-case ones by their reading order rather than by their code points.
    private static Optional<String> fields(Set<ExperimentItemFieldGroup> fields) {
        return Optional.of(fields)
                .filter(groups -> !groups.isEmpty())
                .map(groups -> groups.stream()
                        .map(ExperimentItemFieldGroup::wireName)
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(",")));
    }
}
