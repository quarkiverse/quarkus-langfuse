package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * The criteria Langfuse applies when listing experiments.
 *
 * <p>
 * Instances are immutable and safe to share; build one with {@link #builder()} and derive a variant
 * from an existing one with {@link #toBuilder()}.
 *
 * <pre>{@code
 * var filter = ExperimentFilter.builder()
 *         .datasetId("dataset-1")
 *         .fields(ExperimentFieldGroup.CORE, ExperimentFieldGroup.SCORES)
 *         .build();
 *
 * langfuse.experiments().since(OffsetDateTime.now().minusDays(7)).matching(filter).findAll();
 * }</pre>
 *
 * <p>
 * <strong>The time range is not a criterion here.</strong> Langfuse requires a lower bound on it and
 * answers HTTP 400 without one, so it is taken by {@link ExperimentTimeWindow} when the collection
 * is reached rather than being an optional field on this type. Carrying it in both places would
 * leave a conflict with no defined winner.
 *
 * <p>
 * <strong>Paging is not a criterion.</strong> Langfuse accepts {@code limit} and {@code cursor} on
 * the same endpoint, but those belong to the traversal rather than to the selection: they are
 * supplied from the {@link io.quarkiverse.langfuse.api.cursor.CursorSelection} or
 * {@link io.quarkiverse.langfuse.api.cursor.Cursor} the operation was given. Exposing them here would
 * let a caller fight the traversal.
 *
 * @see ExperimentOperations#matching(ExperimentFilter)
 */
public sealed interface ExperimentFilter permits DefaultExperimentFilter {

    /**
     * A new, empty builder.
     *
     * @return a builder with no criterion set
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * The filter matching every experiment, which is what an unfiltered
     * {@link LangfuseOperations#experiments()} uses.
     *
     * @return a filter with no criterion set
     */
    static ExperimentFilter none() {
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
     * The field groups to return for each experiment.
     *
     * <p>
     * Unlike the other criteria this one does not restrict which experiments match; it widens what
     * each matching experiment carries. An empty set - the default - asks for
     * {@link ExperimentFieldGroup#CORE} alone.
     *
     * @return the field groups to include, empty if only the core fields are wanted. Never
     *         {@code null}
     */
    Set<ExperimentFieldGroup> fields();

    /**
     * How many scores to return per experiment when {@link ExperimentFieldGroup#SCORES} is requested.
     *
     * <p>
     * Langfuse caps this at 50 and defaults to 50.
     *
     * @return the per-experiment score cap, or empty to take the Langfuse default
     */
    Optional<Integer> scoreLimit();

    /**
     * The experiment ids to match, comma-separated.
     *
     * @return the experiment ids to match, or empty if experiments of any id match
     */
    Optional<String> id();

    /**
     * The experiment names to match, comma-separated.
     *
     * @return the experiment names to match, or empty if experiments of any name match
     */
    Optional<String> name();

    /**
     * The dataset ids to match, comma-separated.
     *
     * @return the dataset ids to match, or empty if experiments of any dataset match
     */
    Optional<String> datasetId();

    /**
     * The structured Langfuse filter expression, as JSON.
     *
     * <p>
     * Langfuse supports the {@code id}, {@code name} and {@code datasetId} columns here.
     *
     * @return the filter expression to apply, or empty if the individual criteria should be used
     */
    Optional<String> filter();

    /**
     * Builds an {@link ExperimentFilter}.
     */
    class Builder {
        // Field names mirror the record components of DefaultExperimentFilter one-for-one, so the single
        // positional delegation in its builder-taking constructor reads as matched pairs and a
        // transposition is visible on that line.
        Set<ExperimentFieldGroup> rawFields;
        Integer rawScoreLimit;
        String rawId;
        String rawName;
        String rawDatasetId;
        String rawFilter;

        private Builder() {
        }

        private Builder(ExperimentFilter filter) {
            this.rawFields = filter.fields();
            this.rawScoreLimit = filter.scoreLimit().orElse(null);
            this.rawId = filter.id().orElse(null);
            this.rawName = filter.name().orElse(null);
            this.rawDatasetId = filter.datasetId().orElse(null);
            this.rawFilter = filter.filter().orElse(null);
        }

        /**
         * Asks for the given field groups on each experiment.
         *
         * @param fields the field groups to include, must not contain {@code null}; pass none to ask
         *        for the core fields alone
         * @return this builder
         */
        public Builder fields(ExperimentFieldGroup... fields) {
            // copyOf rather than Set.of: Set.of rejects a repeated group with an IllegalArgumentException,
            // and naming the same group twice is harmless rather than a caller error.
            this.rawFields = (fields == null) ? null : Set.copyOf(Arrays.asList(fields));

            return this;
        }

        /**
         * Asks for the given field groups on each experiment.
         *
         * @param fields the field groups to include, or {@code null} to ask for the core fields alone
         * @return this builder
         */
        public Builder fields(Set<ExperimentFieldGroup> fields) {
            this.rawFields = fields;

            return this;
        }

        /**
         * Caps how many scores are returned per experiment.
         *
         * @param scoreLimit the per-experiment score cap, or {@code null} to take the Langfuse default
         * @return this builder
         */
        public Builder scoreLimit(Integer scoreLimit) {
            this.rawScoreLimit = scoreLimit;

            return this;
        }

        /**
         * Restricts the filter to the experiments with the given ids.
         *
         * @param id the experiment ids to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder id(String id) {
            this.rawId = id;

            return this;
        }

        /**
         * Restricts the filter to the experiments with the given names.
         *
         * @param name the experiment names to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder name(String name) {
            this.rawName = name;

            return this;
        }

        /**
         * Restricts the filter to the experiments run over the given datasets.
         *
         * @param datasetId the dataset ids to match, comma-separated, or {@code null} to match any
         * @return this builder
         */
        public Builder datasetId(String datasetId) {
            this.rawDatasetId = datasetId;

            return this;
        }

        /**
         * Applies the given structured Langfuse filter expression.
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
        public ExperimentFilter build() {
            return new DefaultExperimentFilter(this);
        }
    }
}
