package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * The criteria Langfuse applies when listing experiment items.
 *
 * <p>
 * Instances are immutable and safe to share; build one with {@link #builder()} and derive a variant
 * from an existing one with {@link #toBuilder()}.
 *
 * <pre>{@code
 * var filter = ExperimentItemFilter.builder()
 *         .experimentName("nightly-eval")
 *         .fields(ExperimentItemFieldGroup.CORE, ExperimentItemFieldGroup.IO)
 *         .build();
 *
 * langfuse.experimentItems().since(OffsetDateTime.now().minusDays(7)).matching(filter).findAll();
 * }</pre>
 *
 * <p>
 * <strong>{@link #experimentId()} and {@link #experimentName()} are criteria, not a parent.</strong>
 * Langfuse serves experiment items from a top-level collection and accepts both as comma-separated
 * query parameters, so narrowing to one experiment is a filter like any other rather than a scoping
 * step - and an unnarrowed walk over every experiment's items is a legitimate, supported query.
 *
 * <p>
 * <strong>The time range is not a criterion here.</strong> Langfuse requires a lower bound on it and
 * answers HTTP 400 without one, so it is taken by {@link ExperimentItemTimeWindow} when the
 * collection is reached rather than being an optional field on this type. Carrying it in both places
 * would leave a conflict with no defined winner.
 *
 * <p>
 * <strong>Paging is not a criterion.</strong> Langfuse accepts {@code limit} and {@code cursor} on
 * the same endpoint, but those belong to the traversal rather than to the selection: they are
 * supplied from the {@link io.quarkiverse.langfuse.api.cursor.CursorSelection} or
 * {@link io.quarkiverse.langfuse.api.cursor.Cursor} the operation was given. Exposing them here would
 * let a caller fight the traversal.
 *
 * @see ExperimentItemOperations#matching(ExperimentItemFilter)
 */
public sealed interface ExperimentItemFilter permits DefaultExperimentItemFilter {

    /**
     * A new, empty builder.
     *
     * @return a builder with no criterion set
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * The filter matching every experiment item, which is what an unfiltered
     * {@link LangfuseOperations#experimentItems()} uses.
     *
     * @return a filter with no criterion set
     */
    static ExperimentItemFilter none() {
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
     * The field groups to return for each experiment item.
     *
     * <p>
     * Unlike the other criteria this one does not restrict which items match; it widens what each
     * matching item carries. An empty set - the default - asks for
     * {@link ExperimentItemFieldGroup#CORE} and {@link ExperimentItemFieldGroup#DATASET} together,
     * which is what Langfuse returns when nothing is requested.
     *
     * @return the field groups to include, empty to take the Langfuse default. Never {@code null}
     */
    Set<ExperimentItemFieldGroup> fields();

    /**
     * How many scores to return per experiment item when {@link ExperimentItemFieldGroup#SCORES} is
     * requested.
     *
     * <p>
     * Langfuse caps this at 50 and defaults to 50.
     *
     * @return the per-item score cap, or empty to take the Langfuse default
     */
    Optional<Integer> scoreLimit();

    /**
     * The ids of the experiments whose items should match, comma-separated.
     *
     * @return the experiment ids to match, or empty if items of any experiment match
     */
    Optional<String> experimentId();

    /**
     * The names of the experiments whose items should match, comma-separated.
     *
     * @return the experiment names to match, or empty if items of any experiment match
     */
    Optional<String> experimentName();

    /**
     * The experiment item ids to match, comma-separated.
     *
     * @return the experiment item ids to match, or empty if items of any id match
     */
    Optional<String> experimentItemId();

    /**
     * The dataset ids to match, comma-separated.
     *
     * @return the dataset ids to match, or empty if items of any dataset match
     */
    Optional<String> datasetId();

    /**
     * The structured Langfuse filter expression, as JSON.
     *
     * <p>
     * Langfuse supports the {@code experimentId}, {@code experimentName}, {@code experimentItemId} and
     * {@code datasetId} columns here.
     *
     * @return the filter expression to apply, or empty if the individual criteria should be used
     */
    Optional<String> filter();

    /**
     * Builds an {@link ExperimentItemFilter}.
     */
    class Builder {
        // Field names mirror the record components of DefaultExperimentItemFilter one-for-one, so the
        // single positional delegation in its builder-taking constructor reads as matched pairs and a
        // transposition is visible on that line.
        Set<ExperimentItemFieldGroup> rawFields;
        Integer rawScoreLimit;
        String rawExperimentId;
        String rawExperimentName;
        String rawExperimentItemId;
        String rawDatasetId;
        String rawFilter;

        private Builder() {
        }

        private Builder(ExperimentItemFilter filter) {
            this.rawFields = filter.fields();
            this.rawScoreLimit = filter.scoreLimit().orElse(null);
            this.rawExperimentId = filter.experimentId().orElse(null);
            this.rawExperimentName = filter.experimentName().orElse(null);
            this.rawExperimentItemId = filter.experimentItemId().orElse(null);
            this.rawDatasetId = filter.datasetId().orElse(null);
            this.rawFilter = filter.filter().orElse(null);
        }

        /**
         * Asks for the given field groups on each experiment item.
         *
         * @param fields the field groups to include, must not contain {@code null}; pass none to take
         *        the Langfuse default
         * @return this builder
         */
        public Builder fields(ExperimentItemFieldGroup... fields) {
            // copyOf rather than Set.of: Set.of rejects a repeated group with an IllegalArgumentException,
            // and naming the same group twice is harmless rather than a caller error.
            this.rawFields = (fields == null) ? null : Set.copyOf(Arrays.asList(fields));

            return this;
        }

        /**
         * Asks for the given field groups on each experiment item.
         *
         * @param fields the field groups to include, or {@code null} to take the Langfuse default
         * @return this builder
         */
        public Builder fields(Set<ExperimentItemFieldGroup> fields) {
            this.rawFields = fields;

            return this;
        }

        /**
         * Caps how many scores are returned per experiment item.
         *
         * @param scoreLimit the per-item score cap, or {@code null} to take the Langfuse default
         * @return this builder
         */
        public Builder scoreLimit(Integer scoreLimit) {
            this.rawScoreLimit = scoreLimit;

            return this;
        }

        /**
         * Restricts the filter to the items of the experiments with the given ids.
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
         * Restricts the filter to the items of the experiments with the given names.
         *
         * @param experimentName the experiment names to match, comma-separated, or {@code null} to
         *        match any
         * @return this builder
         */
        public Builder experimentName(String experimentName) {
            this.rawExperimentName = experimentName;

            return this;
        }

        /**
         * Restricts the filter to the experiment items with the given ids.
         *
         * @param experimentItemId the experiment item ids to match, comma-separated, or {@code null} to
         *        match any
         * @return this builder
         */
        public Builder experimentItemId(String experimentItemId) {
            this.rawExperimentItemId = experimentItemId;

            return this;
        }

        /**
         * Restricts the filter to the items drawn from the given datasets.
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
        public ExperimentItemFilter build() {
            return new DefaultExperimentItemFilter(this);
        }
    }
}
