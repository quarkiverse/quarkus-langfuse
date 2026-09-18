package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * The criteria Langfuse applies when listing dataset items.
 *
 * <p>
 * Every criterion is optional, and a filter with none set matches the whole collection. Instances are
 * immutable and safe to share; build one with {@link #builder()} and derive a variant from an
 * existing one with {@link #toBuilder()}.
 *
 * <pre>{@code
 * var filter = DatasetItemFilter.builder()
 *         .datasetName("my-dataset")
 *         .sourceTraceId("trace-1")
 *         .build();
 *
 * langfuse.datasetItems().matching(filter).findAll();
 * }</pre>
 *
 * @see DatasetItemOperations#matching(DatasetItemFilter)
 */
public sealed interface DatasetItemFilter permits DefaultDatasetItemFilter {

    /**
     * A new, empty builder.
     *
     * @return a builder with no criterion set
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * The filter matching every dataset item, which is what an unfiltered
     * {@link LangfuseOperations#datasetItems()} uses.
     *
     * @return a filter with no criterion set
     */
    static DatasetItemFilter none() {
        return new DefaultDatasetItemFilter(null, null, null, null);
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
     * The name of the dataset the items belong to.
     *
     * @return the dataset name to match, or empty if items of any dataset match
     */
    Optional<String> datasetName();

    /**
     * The id of the trace the items were sourced from.
     *
     * @return the source trace id to match, or empty if items from any trace match
     */
    Optional<String> sourceTraceId();

    /**
     * The id of the observation the items were sourced from.
     *
     * @return the source observation id to match, or empty if items from any observation match
     */
    Optional<String> sourceObservationId();

    /**
     * The dataset-item version to match, expressed as the instant it was created.
     *
     * <p>
     * Langfuse types this criterion as a timestamp rather than a version number, and this filter
     * exposes it as it is rather than reinterpreting it.
     *
     * @return the version timestamp to match, or empty if items of any version match
     */
    Optional<OffsetDateTime> version();

    /**
     * Builds a {@link DatasetItemFilter}.
     */
    class Builder {
        // Field names mirror the record components of DefaultDatasetItemFilter one-for-one, so the single
        // positional delegation in its builder-taking constructor reads as matched pairs and a
        // transposition is visible on that line.
        String rawDatasetName;
        String rawSourceTraceId;
        String rawSourceObservationId;
        OffsetDateTime rawVersion;

        private Builder() {
        }

        private Builder(DatasetItemFilter filter) {
            this.rawDatasetName = filter.datasetName().orElse(null);
            this.rawSourceTraceId = filter.sourceTraceId().orElse(null);
            this.rawSourceObservationId = filter.sourceObservationId().orElse(null);
            this.rawVersion = filter.version().orElse(null);
        }

        /**
         * Restricts the filter to the items of the given dataset.
         *
         * @param datasetName the dataset name to match, or {@code null} to match any
         * @return this builder
         */
        public Builder datasetName(String datasetName) {
            this.rawDatasetName = datasetName;

            return this;
        }

        /**
         * Restricts the filter to the items sourced from the given trace.
         *
         * @param sourceTraceId the source trace id to match, or {@code null} to match any
         * @return this builder
         */
        public Builder sourceTraceId(String sourceTraceId) {
            this.rawSourceTraceId = sourceTraceId;

            return this;
        }

        /**
         * Restricts the filter to the items sourced from the given observation.
         *
         * @param sourceObservationId the source observation id to match, or {@code null} to match any
         * @return this builder
         */
        public Builder sourceObservationId(String sourceObservationId) {
            this.rawSourceObservationId = sourceObservationId;

            return this;
        }

        /**
         * Restricts the filter to the items of the given version.
         *
         * @param version the version timestamp to match, or {@code null} to match any
         * @return this builder
         */
        public Builder version(OffsetDateTime version) {
            this.rawVersion = version;

            return this;
        }

        /**
         * Builds the filter.
         *
         * @return the filter holding the criteria set on this builder
         */
        public DatasetItemFilter build() {
            return new DefaultDatasetItemFilter(this);
        }
    }
}
