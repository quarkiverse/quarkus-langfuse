package io.quarkiverse.langfuse.api;

/**
 * A group of experiment item fields Langfuse returns in a listing.
 *
 * <p>
 * Langfuse rejects any group it does not know with HTTP 400, which is why this is an enum rather than
 * a free-form string. Asking for nothing is the same as asking for {@link #CORE} and {@link #DATASET}
 * together.
 *
 * @see ExperimentItemFilter#fields()
 */
public enum ExperimentItemFieldGroup {

    /**
     * The identity, timing and placement fields: id, trace id, start and end time, level, environment,
     * experiment id, experiment name and experiment item id. Returned when no group is requested.
     */
    CORE("core"),

    /**
     * Adds the dataset the experiment ran over: its id, the item version and the experiment
     * description. Returned when no group is requested.
     */
    DATASET("dataset"),

    /**
     * Adds the item input, output and expected output.
     */
    IO("io"),

    /**
     * Adds the trace metadata.
     */
    METADATA("metadata"),

    /**
     * Adds the metadata carried by the dataset item itself.
     */
    ITEM_METADATA("itemMetadata"),

    /**
     * Adds the metadata carried by the parent experiment.
     */
    EXPERIMENT_METADATA("experimentMetadata"),

    /**
     * Adds the scores attached to the item, capped by {@link ExperimentItemFilter#scoreLimit()}.
     */
    SCORES("scores");

    private final String wireName;

    ExperimentItemFieldGroup(String wireName) {
        this.wireName = wireName;
    }

    // Not derived from name(): Langfuse spells two of these groups in camel case and answers HTTP 400
    // for anything else, so the wire spelling is stated rather than computed.
    String wireName() {
        return this.wireName;
    }
}
