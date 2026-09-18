package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.langfuse.api.model.ObservationLevel;
import com.langfuse.api.model.ObservationType;

/**
 * The criteria Langfuse applies when listing observations.
 *
 * <p>
 * Every criterion is optional, and a filter with none set matches the whole collection. Instances are
 * immutable and safe to share; build one with {@link #builder()} and derive a variant from an
 * existing one with {@link #toBuilder()}.
 *
 * <pre>{@code
 * var filter = ObservationFilter.builder()
 *         .traceId("trace-1")
 *         .level(ObservationLevel.ERROR)
 *         .build();
 *
 * langfuse.observations().matching(filter).findAll();
 * }</pre>
 *
 * <p>
 * <strong>Paging is not a criterion.</strong> Langfuse accepts {@code limit} and {@code cursor} on
 * the same endpoint, but those belong to the traversal rather than to the selection: they are
 * supplied from the {@link io.quarkiverse.langfuse.api.cursor.CursorSelection} or
 * {@link io.quarkiverse.langfuse.api.cursor.Cursor} the operation was given. Exposing them here would
 * let a caller fight the traversal.
 *
 * <p>
 * <strong>{@code parseIoAsJson} is not exposed either.</strong> Langfuse documents it as deprecated
 * and answers {@code 400} when it is set to {@code true}; input and output fields are always returned
 * as raw strings.
 *
 * @see ObservationOperations#matching(ObservationFilter)
 */
public sealed interface ObservationFilter permits DefaultObservationFilter {

    /**
     * A new, empty builder.
     *
     * @return a builder with no criterion set
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * The filter matching every observation, which is what an unfiltered
     * {@link LangfuseOperations#observations()} uses.
     *
     * @return a filter with no criterion set
     */
    static ObservationFilter none() {
        return new DefaultObservationFilter(null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null);
    }

    /**
     * A builder pre-populated with this filter's criteria, for deriving a variant of it.
     *
     * @return a builder holding this filter's criteria
     */
    default Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * The field groups Langfuse should include in each observation, as a comma-separated list.
     *
     * <p>
     * The groups Langfuse documents are {@code core}, {@code basic}, {@code time}, {@code io},
     * {@code metadata}, {@code model}, {@code usage}, {@code prompt}, {@code metrics} and
     * {@code trace_context}; {@code core} and {@code basic} are returned when nothing is requested.
     * The value is passed through as given rather than being modelled as a closed set, because
     * Langfuse types it as free-form text and adds groups over time.
     *
     * @return the field groups to request, or empty to take the Langfuse default
     */
    Optional<String> fields();

    /**
     * The metadata keys Langfuse should return in full, as a comma-separated list.
     *
     * <p>
     * Metadata values longer than 200 characters are truncated unless their key is listed here.
     *
     * @return the metadata keys to return untruncated, or empty to accept truncation throughout
     */
    Optional<String> expandMetadata();

    /**
     * The observation name to match.
     *
     * @return the name to match, or empty if observations of any name match
     */
    Optional<String> name();

    /**
     * The id of the user the observations were recorded for.
     *
     * @return the user id to match, or empty if observations of any user match
     */
    Optional<String> userId();

    /**
     * The id of the session the observations belong to.
     *
     * @return the session id to match, or empty if observations of any session match
     */
    Optional<String> sessionId();

    /**
     * The kind of observation to match.
     *
     * @return the observation type to match, or empty if observations of any type match
     */
    Optional<ObservationType> type();

    /**
     * The id of the trace the observations belong to.
     *
     * @return the trace id to match, or empty if observations of any trace match
     */
    Optional<String> traceId();

    /**
     * The severity level to match.
     *
     * @return the level to match, or empty if observations of any level match
     */
    Optional<ObservationLevel> level();

    /**
     * The id of the observation the matches must be direct children of.
     *
     * @return the parent observation id to match, or empty if observations of any parent match
     */
    Optional<String> parentObservationId();

    /**
     * Whether to match only root observations, which are those with no parent.
     *
     * @return {@code true} to match only roots, {@code false} to match only non-roots, or empty if
     *         both match
     */
    Optional<Boolean> isRootObservation();

    /**
     * The environments the observations were recorded in, any one of which matches.
     *
     * <p>
     * Unlike the other criteria this is a list rather than an {@link Optional}, because Langfuse
     * accepts the parameter repeatedly. An empty list means no environment restriction.
     *
     * @return the environments to match, never {@code null} and never modifiable
     */
    List<String> environment();

    /**
     * The earliest start time to match, inclusive.
     *
     * @return the lower bound on the start time, or empty if there is no lower bound
     */
    Optional<OffsetDateTime> fromStartTime();

    /**
     * The latest start time to match, exclusive.
     *
     * @return the upper bound on the start time, or empty if there is no upper bound
     */
    Optional<OffsetDateTime> toStartTime();

    /**
     * The observation version to match.
     *
     * @return the version to match, or empty if observations of any version match
     */
    Optional<String> version();

    /**
     * The structured Langfuse filter expression, as JSON.
     *
     * <p>
     * <strong>Langfuse gives this precedence over every other criterion on this filter.</strong> When
     * it is set, the individual criteria above are ignored server-side rather than combined with it.
     *
     * @return the filter expression to apply, or empty if the individual criteria should be used
     */
    Optional<String> filter();

    /**
     * Builds an {@link ObservationFilter}.
     */
    class Builder {
        // Field names mirror the record components of DefaultObservationFilter one-for-one, so the single
        // positional delegation in its builder-taking constructor reads as matched pairs and a
        // transposition is visible on that line.
        String rawFields;
        String rawExpandMetadata;
        String rawName;
        String rawUserId;
        String rawSessionId;
        ObservationType rawType;
        String rawTraceId;
        ObservationLevel rawLevel;
        String rawParentObservationId;
        Boolean rawIsRootObservation;
        List<String> rawEnvironment;
        OffsetDateTime rawFromStartTime;
        OffsetDateTime rawToStartTime;
        String rawVersion;
        String rawFilter;

        private Builder() {
        }

        private Builder(ObservationFilter filter) {
            this.rawFields = filter.fields().orElse(null);
            this.rawExpandMetadata = filter.expandMetadata().orElse(null);
            this.rawName = filter.name().orElse(null);
            this.rawUserId = filter.userId().orElse(null);
            this.rawSessionId = filter.sessionId().orElse(null);
            this.rawType = filter.type().orElse(null);
            this.rawTraceId = filter.traceId().orElse(null);
            this.rawLevel = filter.level().orElse(null);
            this.rawParentObservationId = filter.parentObservationId().orElse(null);
            this.rawIsRootObservation = filter.isRootObservation().orElse(null);
            // environment() answers an empty list for "unset", so it is folded back to null here: keeping
            // the empty list would make toBuilder().build() unequal to the filter it came from.
            this.rawEnvironment = filter.environment().isEmpty() ? null : filter.environment();
            this.rawFromStartTime = filter.fromStartTime().orElse(null);
            this.rawToStartTime = filter.toStartTime().orElse(null);
            this.rawVersion = filter.version().orElse(null);
            this.rawFilter = filter.filter().orElse(null);
        }

        /**
         * Restricts the response to the given comma-separated field groups.
         *
         * @param fields the field groups to request, or {@code null} to take the Langfuse default
         * @return this builder
         */
        public Builder fields(String fields) {
            this.rawFields = fields;

            return this;
        }

        /**
         * Asks Langfuse to return the given comma-separated metadata keys untruncated.
         *
         * @param expandMetadata the metadata keys to expand, or {@code null} to accept truncation
         * @return this builder
         */
        public Builder expandMetadata(String expandMetadata) {
            this.rawExpandMetadata = expandMetadata;

            return this;
        }

        /**
         * Restricts the filter to the observations with the given name.
         *
         * @param name the name to match, or {@code null} to match any
         * @return this builder
         */
        public Builder name(String name) {
            this.rawName = name;

            return this;
        }

        /**
         * Restricts the filter to the observations recorded for the given user.
         *
         * @param userId the user id to match, or {@code null} to match any
         * @return this builder
         */
        public Builder userId(String userId) {
            this.rawUserId = userId;

            return this;
        }

        /**
         * Restricts the filter to the observations of the given session.
         *
         * @param sessionId the session id to match, or {@code null} to match any
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.rawSessionId = sessionId;

            return this;
        }

        /**
         * Restricts the filter to the observations of the given kind.
         *
         * @param type the observation type to match, or {@code null} to match any
         * @return this builder
         */
        public Builder type(ObservationType type) {
            this.rawType = type;

            return this;
        }

        /**
         * Restricts the filter to the observations of the given trace.
         *
         * @param traceId the trace id to match, or {@code null} to match any
         * @return this builder
         */
        public Builder traceId(String traceId) {
            this.rawTraceId = traceId;

            return this;
        }

        /**
         * Restricts the filter to the observations of the given severity level.
         *
         * @param level the level to match, or {@code null} to match any
         * @return this builder
         */
        public Builder level(ObservationLevel level) {
            this.rawLevel = level;

            return this;
        }

        /**
         * Restricts the filter to the direct children of the given observation.
         *
         * @param parentObservationId the parent observation id to match, or {@code null} to match any
         * @return this builder
         */
        public Builder parentObservationId(String parentObservationId) {
            this.rawParentObservationId = parentObservationId;

            return this;
        }

        /**
         * Restricts the filter to root observations, or to non-root ones.
         *
         * @param isRootObservation {@code true} for roots only, {@code false} for non-roots only, or
         *        {@code null} to match both
         * @return this builder
         */
        public Builder isRootObservation(Boolean isRootObservation) {
            this.rawIsRootObservation = isRootObservation;

            return this;
        }

        /**
         * Restricts the filter to the observations recorded in any of the given environments.
         *
         * <p>
         * The list is copied, so later changes to the argument do not affect the built filter.
         *
         * @param environment the environments to match, or {@code null} or empty to match any; must not
         *        contain {@code null} elements
         * @return this builder
         * @throws NullPointerException if any element of {@code environment} is {@code null}
         */
        public Builder environment(List<String> environment) {
            this.rawEnvironment = ((environment == null) || environment.isEmpty()) ? null : List.copyOf(environment);

            return this;
        }

        /**
         * Restricts the filter to the observations started at or after the given instant.
         *
         * @param fromStartTime the inclusive lower bound on the start time, or {@code null} for none
         * @return this builder
         */
        public Builder fromStartTime(OffsetDateTime fromStartTime) {
            this.rawFromStartTime = fromStartTime;

            return this;
        }

        /**
         * Restricts the filter to the observations started before the given instant.
         *
         * @param toStartTime the exclusive upper bound on the start time, or {@code null} for none
         * @return this builder
         */
        public Builder toStartTime(OffsetDateTime toStartTime) {
            this.rawToStartTime = toStartTime;

            return this;
        }

        /**
         * Restricts the filter to the observations of the given version.
         *
         * @param version the version to match, or {@code null} to match any
         * @return this builder
         */
        public Builder version(String version) {
            this.rawVersion = version;

            return this;
        }

        /**
         * Applies the given structured Langfuse filter expression, which takes precedence over every
         * other criterion on this builder.
         *
         * @param filter the filter expression as JSON, or {@code null} to use the individual criteria
         * @return this builder
         */
        public Builder filter(String filter) {
            this.rawFilter = filter;

            return this;
        }

        /**
         * Builds the filter.
         *
         * @return the filter holding the criteria set on this builder
         */
        public ObservationFilter build() {
            return new DefaultObservationFilter(this);
        }
    }
}
