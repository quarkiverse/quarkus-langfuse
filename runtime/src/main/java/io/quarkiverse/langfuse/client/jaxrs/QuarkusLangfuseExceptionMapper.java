package io.quarkiverse.langfuse.client.jaxrs;

import java.util.Optional;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import com.langfuse.api.LangfuseApiException;

import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;

public class QuarkusLangfuseExceptionMapper implements ResponseExceptionMapper<LangfuseApiException> {

    @Override
    public LangfuseApiException toThrowable(Response response) {
        // The entity is read once up front: the response body is a stream, so a second read would
        // see it already consumed.
        var status = response.getStatus();
        var message = "Langfuse API error (%d): %s".formatted(status, response.readEntity(String.class));

        // fromStatusCode answers null for any code outside the Jakarta RS enum - nginx's 499 and
        // Cloudflare's 520-526 reach us that way through a proxy - so the unknown case is folded into
        // the same generic failure as the statuses that have no dedicated type.
        var knownStatus = Status.fromStatusCode(status);

        // Never returns null. A null return tells the REST Client the response was not an error, which
        // makes it deserialize the error body into the declared return type - the defect this mapper
        // used to have for every non-404 client error.
        return Optional.ofNullable(knownStatus)
                .map(s -> toApiException(s, message))
                .orElseGet(() -> new LangfuseApiException(message, status));
    }

    private static LangfuseApiException toApiException(Status status, String message) {
        return switch (status) {
            case UNAUTHORIZED -> new LangfuseAuthenticationException(message);
            case FORBIDDEN -> new LangfuseAuthorizationException(message);
            case NOT_FOUND -> new LangfuseNotFoundException(message);
            default -> new LangfuseApiException(message, status.getStatusCode());
        };
    }
}
