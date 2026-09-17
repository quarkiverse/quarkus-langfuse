package io.quarkiverse.langfuse.client;

import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.LangfuseErrorBody;

/**
 * Thrown when Langfuse rejects the request credentials, corresponding to HTTP {@code 401}.
 *
 * <p>
 * No identity was established: the API keys were missing, malformed, or not recognised. Check
 * {@code quarkus.langfuse.public-key} and {@code quarkus.langfuse.secret-key}, and that they belong
 * to the project the configured {@code quarkus.langfuse.base-url} points at.
 *
 * <p>
 * This is distinct from {@link LangfuseAuthorizationException}, which means the credentials were
 * accepted but the action was not permitted. Catch {@link LangfuseApiException} to handle both
 * together.
 */
public class LangfuseAuthenticationException extends LangfuseApiException {

    /**
     * @param message the error message
     */
    public LangfuseAuthenticationException(String message) {
        super(message, 401);
    }

    /**
     * @param message the error message
     * @param errorBody the response body the server sent, or {@link LangfuseErrorBody#empty()} when
     *        there was none
     */
    public LangfuseAuthenticationException(String message, LangfuseErrorBody errorBody) {
        super(message, 401, errorBody);
    }
}
