package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.ObservationV2;
import com.langfuse.api.model.ObservationsV2Meta;
import com.langfuse.api.observations.ObservationsApi.APIObservationsGetManyRequest;
import com.langfuse.api.observations.async.ObservationsApi;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncObservationOperations extends AbstractAsyncCursorOperations<ObservationV2>
        implements AsyncObservationOperations {
    private final ObservationsApi observationsApi;
    private final LangfuseConfig config;

    DefaultAsyncObservationOperations(ObservationsApi observationsApi, LangfuseConfig config) {
        this(observationsApi, config, ObservationFilter.none());
    }

    // The filter is captured in the fetcher lambda handed to the engine, which is why a filtered view
    // needs neither a widened AsyncCursorFetcher nor a change to AsyncPagination: a view is just this
    // class constructed over a different fetcher.
    private DefaultAsyncObservationOperations(ObservationsApi observationsApi, LangfuseConfig config,
            ObservationFilter filter) {
        super(cursor -> fetch(observationsApi, cursor, filter), config);
        this.observationsApi = observationsApi;
        this.config = config;
    }

    @Override
    public AsyncObservationOperations matching(ObservationFilter filter) {
        return new DefaultAsyncObservationOperations(this.observationsApi, this.config,
                ValidationUtils.ensureNotNull(filter, "Filter"));
    }

    private static Uni<CursorResult<ObservationV2>> fetch(ObservationsApi observationsApi, Cursor cursor,
            ObservationFilter filter) {
        // Built outside the supplier: assembling the request reads nothing that has to be deferred, and
        // the supplier overload is used purely so the call itself starts at subscription rather than
        // assembly time.
        var request = request(cursor, filter);

        return Uni.createFrom()
                .completionStage(() -> observationsApi.observationsGetMany(request))
                .map(response -> CursorResults.from(cursor, response.getData(), response.getMeta(),
                        ObservationsV2Meta::getCursor));
    }

    // limit and cursor come from the traversal rather than from the filter, which is why ObservationFilter
    // does not carry them. type and level are typed enums here and are mapped to what the generated
    // builder wants at this boundary: level keeps its enum, type is widened to its name.
    private static APIObservationsGetManyRequest request(Cursor cursor, ObservationFilter filter) {
        var builder = APIObservationsGetManyRequest.newBuilder()
                .limit(cursor.limit())
                .cursor(cursor.value().orElse(null));

        filter.fields().ifPresent(builder::fields);
        filter.expandMetadata().ifPresent(builder::expandMetadata);
        filter.name().ifPresent(builder::name);
        filter.userId().ifPresent(builder::userId);
        filter.sessionId().ifPresent(builder::sessionId);
        filter.type().map(Enum::name).ifPresent(builder::type);
        filter.traceId().ifPresent(builder::traceId);
        filter.level().ifPresent(builder::level);
        filter.parentObservationId().ifPresent(builder::parentObservationId);
        filter.isRootObservation().ifPresent(builder::isRootObservation);
        filter.fromStartTime().ifPresent(builder::fromStartTime);
        filter.toStartTime().ifPresent(builder::toStartTime);
        filter.version().ifPresent(builder::version);
        filter.filter().ifPresent(builder::filter);

        // environment is a list rather than an Optional, and an empty one means "unrestricted": left
        // unset so the parameter is omitted entirely rather than sent empty.
        if (!filter.environment().isEmpty()) {
            builder.environment(filter.environment());
        }

        return builder.build();
    }
}
