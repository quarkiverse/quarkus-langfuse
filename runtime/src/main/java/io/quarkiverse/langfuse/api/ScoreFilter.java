package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.ScoreSource;

/**
 * The criteria Langfuse applies when listing scores.
 *
 * <p>
 * Every criterion is optional, and a filter with none set matches the whole collection. Instances are
 * immutable and safe to share; build one with {@link #builder()} and derive a variant from an
 * existing one with {@link #toBuilder()}.
 *
 * <pre>{@code
 * var filter = ScoreFilter.builder()
 *         .traceId("trace-1")
 *         .dataType(ScoreDataType.NUMERIC)
 *         .valueMin(0.5d)
 *         .fields(ScoreFieldGroup.DETAILS, ScoreFieldGroup.SUBJECT)
 *         .build();
 *
 * langfuse.scores().matching(filter).findAll();
 * }</pre>
 *
 * <p>
 * <strong>Several criteria are multi-valued.</strong> Langfuse reads {@link #id()}, {@link #name()},
 * {@link #environment()}, {@link #configId()}, {@link #queueId()}, {@link #authorUserId()},
 * {@link #value()}, {@link #traceId()}, {@link #sessionId()}, {@link #observationId()} and
 * {@link #experimentId()} as comma-separated lists, matching any of the listed values and combining
 * across criteria with AND. They are exposed as they are typed on the wire rather than reinterpreted,
 * so a caller wanting several values passes them comma-separated.
 *
 * <p>
 * <strong>Langfuse enforces combination rules server-side, and this filter does not pre-empt
 * them.</strong> {@link #value()}, {@link #valueMin()} and {@link #valueMax()} require a single
 * {@link #dataType()}; the bounds require {@code NUMERIC}; {@link #observationId()} requires
 * {@link #traceId()}; and {@link #traceId()}, {@link #sessionId()} and {@link #experimentId()} are
 * mutually exclusive. A combination Langfuse rejects surfaces as a
 * {@link com.langfuse.api.LangfuseApiException} carrying HTTP 400 when the listing runs, not as an
 * {@link IllegalArgumentException} from the builder.
 *
 * @see ScoreOperations#matching(ScoreFilter)
 */
public sealed interface ScoreFilter permits DefaultScoreFilter {

    /**
     * A new, empty builder.
     *
     * @return a builder with no criterion set
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * The filter matching every score, which is what an unfiltered
     * {@link LangfuseOperations#scores()} uses.
     *
     * @return a filter with no criterion set
     */
    static ScoreFilter none() {
        return builder().build();
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
     * The optional field groups to return in addition to the core score fields.
     *
     * <p>
     * Unlike the other criteria this one does not restrict which scores match; it widens what each
     * matching score carries. An empty set - the default - asks for the core fields alone.
     *
     * @return the field groups to include, empty if only the core fields are wanted. Never
     *         {@code null}
     */
    Set<ScoreFieldGroup> fields();

    /**
     * The score ids to match, comma-separated.
     *
     * @return the score ids to match, or empty if scores of any id match
     */
    Optional<String> id();

    /**
     * The score names to match, comma-separated.
     *
     * @return the score names to match, or empty if scores of any name match
     */
    Optional<String> name();

    /**
     * How the score was produced.
     *
     * @return the source to match, or empty if scores of any source match
     */
    Optional<ScoreSource> source();

    /**
     * The type of the score's value.
     *
     * @return the data type to match, or empty if scores of any data type match
     */
    Optional<ScoreDataType> dataType();

    /**
     * The environments to match, comma-separated.
     *
     * @return the environments to match, or empty if scores of any environment match
     */
    Optional<String> environment();

    /**
     * The score config ids to match, comma-separated.
     *
     * @return the score config ids to match, or empty if scores of any config match
     */
    Optional<String> configId();

    /**
     * The annotation queue ids to match, comma-separated.
     *
     * @return the annotation queue ids to match, or empty if scores of any queue match
     */
    Optional<String> queueId();

    /**
     * The ids of the users who authored the score, comma-separated.
     *
     * @return the author user ids to match, or empty if scores of any author match
     */
    Optional<String> authorUserId();

    /**
     * The exact score values to match, comma-separated.
     *
     * <p>
     * The values are carried as text whatever the {@link #dataType()} is: Langfuse expects
     * {@code true}/{@code false} for {@code BOOLEAN} and a finite number for {@code NUMERIC}, and
     * rejects anything else with HTTP 400.
     *
     * @return the values to match, or empty if scores of any value match
     */
    Optional<String> value();

    /**
     * The inclusive lower bound on a numeric score's value.
     *
     * @return the lower bound to match, or empty if scores of any value match
     */
    Optional<Double> valueMin();

    /**
     * The inclusive upper bound on a numeric score's value.
     *
     * @return the upper bound to match, or empty if scores of any value match
     */
    Optional<Double> valueMax();

    /**
     * The ids of the traces the scores are attached to, comma-separated.
     *
     * @return the trace ids to match, or empty if scores of any trace match
     */
    Optional<String> traceId();

    /**
     * The ids of the sessions the scores are attached to, comma-separated.
     *
     * @return the session ids to match, or empty if scores of any session match
     */
    Optional<String> sessionId();

    /**
     * The ids of the observations the scores are attached to, comma-separated.
     *
     * <p>
     * Observation ids are scoped to a trace, so Langfuse requires {@link #traceId()} alongside this.
     *
     * @return the observation ids to match, or empty if scores of any observation match
     */
    Optional<String> observationId();

    /**
     * The ids of the experiments - dataset runs - the scores are attached to, comma-separated.
     *
     * @return the experiment ids to match, or empty if scores of any experiment match
     */
    Optional<String> experimentId();

    /**
     * The inclusive lower bound on the score timestamp.
     *
     * @return the earliest timestamp to match, or empty if scores of any timestamp match
     */
    Optional<OffsetDateTime> fromTimestamp();

    /**
     * The exclusive upper bound on the score timestamp.
     *
     * @return the timestamp to match up to, or empty if scores of any timestamp match
     */
    Optional<OffsetDateTime> toTimestamp();

    /**
     * Builds a {@link ScoreFilter}.
     */
    class Builder {
        // Field names mirror the record components of DefaultScoreFilter one-for-one, so the single
        // positional delegation in its builder-taking constructor reads as matched pairs and a
        // transposition is visible on that line. At eighteen components that is the only review
        // technique that scales.
        Set<ScoreFieldGroup> rawFields;
        String rawId;
        String rawName;
        ScoreSource rawSource;
        ScoreDataType rawDataType;
        String rawEnvironment;
        String rawConfigId;
        String rawQueueId;
        String rawAuthorUserId;
        String rawValue;
        Double rawValueMin;
        Double rawValueMax;
        String rawTraceId;
        String rawSessionId;
        String rawObservationId;
        String rawExperimentId;
        OffsetDateTime rawFromTimestamp;
        OffsetDateTime rawToTimestamp;

        private Builder() {
        }

        private Builder(ScoreFilter filter) {
            this.rawFields = filter.fields();
            this.rawId = filter.id().orElse(null);
            this.rawName = filter.name().orElse(null);
            this.rawSource = filter.source().orElse(null);
            this.rawDataType = filter.dataType().orElse(null);
            this.rawEnvironment = filter.environment().orElse(null);
            this.rawConfigId = filter.configId().orElse(null);
            this.rawQueueId = filter.queueId().orElse(null);
            this.rawAuthorUserId = filter.authorUserId().orElse(null);
            this.rawValue = filter.value().orElse(null);
            this.rawValueMin = filter.valueMin().orElse(null);
            this.rawValueMax = filter.valueMax().orElse(null);
            this.rawTraceId = filter.traceId().orElse(null);
            this.rawSessionId = filter.sessionId().orElse(null);
            this.rawObservationId = filter.observationId().orElse(null);
            this.rawExperimentId = filter.experimentId().orElse(null);
            this.rawFromTimestamp = filter.fromTimestamp().orElse(null);
            this.rawToTimestamp = filter.toTimestamp().orElse(null);
        }

        /**
         * Asks for the given field groups in addition to the core score fields.
         *
         * @param fields the field groups to include, must not contain {@code null}; pass none to ask
         *        for the core fields alone
         * @return this builder
         */
        public Builder fields(ScoreFieldGroup... fields) {
            // copyOf rather than Set.of: Set.of rejects a repeated group with an IllegalArgumentException,
            // and naming the same group twice is harmless rather than a caller error.
            this.rawFields = (fields == null) ? null : Set.copyOf(Arrays.asList(fields));

            return this;
        }

        /**
         * Asks for the given field groups in addition to the core score fields.
         *
         * @param fields the field groups to include, or {@code null} to ask for the core fields alone
         * @return this builder
         */
        public Builder fields(Set<ScoreFieldGroup> fields) {
            this.rawFields = fields;

            return this;
        }

        /**
         * Restricts the filter to the scores with the given ids.
         *
         * @param id the score ids to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder id(String id) {
            this.rawId = id;

            return this;
        }

        /**
         * Restricts the filter to the scores with the given names.
         *
         * @param name the score names to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder name(String name) {
            this.rawName = name;

            return this;
        }

        /**
         * Restricts the filter to the scores produced by the given source.
         *
         * @param source the source to match, or {@code null} to match any
         * @return this builder
         */
        public Builder source(ScoreSource source) {
            this.rawSource = source;

            return this;
        }

        /**
         * Restricts the filter to the scores whose value has the given type.
         *
         * @param dataType the data type to match, or {@code null} to match any
         * @return this builder
         */
        public Builder dataType(ScoreDataType dataType) {
            this.rawDataType = dataType;

            return this;
        }

        /**
         * Restricts the filter to the scores recorded in the given environments.
         *
         * @param environment the environments to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder environment(String environment) {
            this.rawEnvironment = environment;

            return this;
        }

        /**
         * Restricts the filter to the scores backed by the given score configs.
         *
         * @param configId the score config ids to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder configId(String configId) {
            this.rawConfigId = configId;

            return this;
        }

        /**
         * Restricts the filter to the scores annotated through the given queues.
         *
         * @param queueId the annotation queue ids to match, comma-separated, or {@code null} to match
         *        any
         * @return this builder
         */
        public Builder queueId(String queueId) {
            this.rawQueueId = queueId;

            return this;
        }

        /**
         * Restricts the filter to the scores authored by the given users.
         *
         * @param authorUserId the author user ids to match, comma-separated, or {@code null} to match
         *        any
         * @return this builder
         */
        public Builder authorUserId(String authorUserId) {
            this.rawAuthorUserId = authorUserId;

            return this;
        }

        /**
         * Restricts the filter to the scores holding one of the given values.
         *
         * @param value the values to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder value(String value) {
            this.rawValue = value;

            return this;
        }

        /**
         * Restricts the filter to the scores whose numeric value is at least {@code valueMin}.
         *
         * @param valueMin the inclusive lower bound to match, or {@code null} to match any
         * @return this builder
         */
        public Builder valueMin(Double valueMin) {
            this.rawValueMin = valueMin;

            return this;
        }

        /**
         * Restricts the filter to the scores whose numeric value is at most {@code valueMax}.
         *
         * @param valueMax the inclusive upper bound to match, or {@code null} to match any
         * @return this builder
         */
        public Builder valueMax(Double valueMax) {
            this.rawValueMax = valueMax;

            return this;
        }

        /**
         * Restricts the filter to the scores attached to the given traces.
         *
         * @param traceId the trace ids to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder traceId(String traceId) {
            this.rawTraceId = traceId;

            return this;
        }

        /**
         * Restricts the filter to the scores attached to the given sessions.
         *
         * @param sessionId the session ids to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.rawSessionId = sessionId;

            return this;
        }

        /**
         * Restricts the filter to the scores attached to the given observations.
         *
         * @param observationId the observation ids to match, comma-separated, or {@code null} to match
         *        any
         * @return this builder
         */
        public Builder observationId(String observationId) {
            this.rawObservationId = observationId;

            return this;
        }

        /**
         * Restricts the filter to the scores attached to the given experiments.
         *
         * @param experimentId the experiment ids to match, comma-separated, or {@code null} to match
         *        any
         * @return this builder
         */
        public Builder experimentId(String experimentId) {
            this.rawExperimentId = experimentId;

            return this;
        }

        /**
         * Restricts the filter to the scores recorded at or after {@code fromTimestamp}.
         *
         * @param fromTimestamp the inclusive lower bound to match, or {@code null} to match any
         * @return this builder
         */
        public Builder fromTimestamp(OffsetDateTime fromTimestamp) {
            this.rawFromTimestamp = fromTimestamp;

            return this;
        }

        /**
         * Restricts the filter to the scores recorded before {@code toTimestamp}.
         *
         * @param toTimestamp the exclusive upper bound to match, or {@code null} to match any
         * @return this builder
         */
        public Builder toTimestamp(OffsetDateTime toTimestamp) {
            this.rawToTimestamp = toTimestamp;

            return this;
        }

        /**
         * Builds the filter.
         *
         * @return the filter holding the criteria set on this builder
         */
        public ScoreFilter build() {
            return new DefaultScoreFilter(this);
        }
    }
}
