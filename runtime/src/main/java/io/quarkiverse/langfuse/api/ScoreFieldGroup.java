package io.quarkiverse.langfuse.api;

/**
 * An optional group of score fields Langfuse returns in addition to the core ones.
 *
 * <p>
 * A score listing always carries the core fields - id, project id, name, value, data type, source,
 * timestamp, environment and the created/updated instants. Each group named here adds more on top,
 * and Langfuse rejects any group it does not know with HTTP 400, which is why this is an enum rather
 * than a free-form string.
 *
 * @see ScoreFilter#fields()
 */
public enum ScoreFieldGroup {

    /**
     * Adds the comment, the config id and the metadata.
     */
    DETAILS,

    /**
     * Adds the subject the score is attached to: its kind (trace, observation, session or
     * experiment), its id, and the trace id for observation-level scores.
     */
    SUBJECT,

    /**
     * Adds the author user id and the annotation queue id.
     */
    ANNOTATION
}
