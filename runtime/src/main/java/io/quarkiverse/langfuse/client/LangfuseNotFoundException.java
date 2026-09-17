package io.quarkiverse.langfuse.client;

import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.LangfuseErrorBody;

/**
 * Thrown when Langfuse answers a request with HTTP {@code 404}.
 *
 * <p>
 * This is the only failure the higher-level operations layer treats as <em>absence</em> rather than
 * as an error: lookups translate it into an empty {@link java.util.Optional} or a {@code null}
 * item, and delete operations translate it into a {@code NotFound} outcome. Every other failure
 * propagates.
 *
 * <p>
 * <strong>A {@code 404} does not always mean the resource is missing.</strong> Langfuse also answers
 * {@code 404} for some refusals - deleting a Langfuse-managed model reports
 * {@code "No model with this id found. Note: You cannot delete built-in models..."} even though the
 * model plainly exists. "You may not delete this" and "this does not exist" are therefore
 * indistinguishable by status code on those endpoints, so do not read this exception as proof of
 * absence.
 */
public class LangfuseNotFoundException extends LangfuseApiException {

    /**
     * @param message the error message
     */
    public LangfuseNotFoundException(String message) {
        super(message, 404);
    }

    /**
     * @param message the error message
     * @param errorBody the response body the server sent, or {@link LangfuseErrorBody#empty()} when
     *        there was none
     */
    public LangfuseNotFoundException(String message, LangfuseErrorBody errorBody) {
        super(message, 404, errorBody);
    }
}
