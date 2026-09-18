package io.quarkiverse.langfuse.api;

/**
 * A group of experiment fields Langfuse returns in a listing.
 *
 * <p>
 * Langfuse rejects any group it does not know with HTTP 400, which is why this is an enum rather than
 * a free-form string. Asking for nothing is the same as asking for {@link #CORE} alone.
 *
 * @see ExperimentFilter#fields()
 */
public enum ExperimentFieldGroup {

    /**
     * The identity, timing and item-count fields: id, name, description, start and end time, item
     * count and dataset id. Returned when no group is requested.
     */
    CORE("core"),

    /**
     * Adds the experiment metadata.
     */
    METADATA("metadata"),

    /**
     * Adds the scores attached directly to the experiment, capped by
     * {@link ExperimentFilter#scoreLimit()}.
     */
    SCORES("scores");

    private final String wireName;

    ExperimentFieldGroup(String wireName) {
        this.wireName = wireName;
    }

    // Not derived from name(): Langfuse spells the groups in its own casing and answers HTTP 400 for
    // anything else, so the wire spelling is stated rather than computed.
    String wireName() {
        return this.wireName;
    }
}
