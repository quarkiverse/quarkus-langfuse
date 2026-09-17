package io.quarkiverse.langfuse.client;

import com.langfuse.api.LangfuseApiException;

/**
 * Thrown when Langfuse accepts the request credentials but refuses the action, corresponding to
 * HTTP {@code 403}.
 *
 * <p>
 * The identity was established: the API keys are valid, but the principal they identify may not
 * perform this operation. Changing the keys does not help unless the replacements carry different
 * permissions - review the key's scope or role, or the project and organization it grants access
 * to. Some Langfuse endpoints are also gated behind a paid plan and answer {@code 403} regardless
 * of which credentials are supplied.
 *
 * <p>
 * This is distinct from {@link LangfuseAuthenticationException}, which means no identity was
 * established at all. Catch {@link LangfuseApiException} to handle both together.
 */
public class LangfuseAuthorizationException extends LangfuseApiException {

    /**
     * @param message the error message
     */
    public LangfuseAuthorizationException(String message) {
        super(message, 403);
    }
}
