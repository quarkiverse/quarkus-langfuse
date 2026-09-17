package com.langfuse.api;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Runtime exception thrown when a Langfuse API call fails.
 *
 * <p>
 * This is an unchecked exception so that the {@link LangfuseApi} interface methods
 * do not force callers to handle checked exceptions. Implementations convert
 * transport-specific checked exceptions into this type.
 *
 * @author Eric Deandrea
 */
public class LangfuseApiException extends RuntimeException {

    private final int statusCode;
    private final LangfuseErrorBody errorBody;

    /**
     * @param message the error message
     */
    public LangfuseApiException(String message) {
        this(message, 0, LangfuseErrorBody.empty());
    }

    /**
     * @param message the error message
     * @param cause the underlying cause
     */
    public LangfuseApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorBody = LangfuseErrorBody.empty();
    }

    /**
     * @param message the error message
     * @param statusCode the HTTP status code returned by the server
     */
    public LangfuseApiException(String message, int statusCode) {
        this(message, statusCode, LangfuseErrorBody.empty());
    }

    /**
     * @param message the error message
     * @param statusCode the HTTP status code returned by the server
     * @param errorBody the response body the server sent, or {@link LangfuseErrorBody#empty()} when
     *        there was none
     */
    public LangfuseApiException(String message, int statusCode, LangfuseErrorBody errorBody) {
        super(message);
        this.statusCode = statusCode;
        this.errorBody = (errorBody != null) ? errorBody : LangfuseErrorBody.empty();
    }

    /**
     * @param message the error message
     * @param statusCode the HTTP status code returned by the server
     * @param cause the underlying cause
     */
    public LangfuseApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorBody = LangfuseErrorBody.empty();
    }

    /**
     * @param cause the underlying cause
     */
    public LangfuseApiException(Throwable cause) {
        super(cause);
        this.statusCode = 0;
        this.errorBody = LangfuseErrorBody.empty();
    }

    /**
     * Returns the HTTP status code from the failed API call, or {@code 0} if not applicable.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the server's own explanation of the failure, without the status code prefix and
     * without the surrounding JSON that {@link #getMessage()} carries.
     *
     * <p>
     * This is the accessor to reach for in a {@code catch (LangfuseApiException e)} block:
     *
     * <pre>{@code
     * try {
     *     datasets.getById("missing");
     * } catch (LangfuseApiException e) {
     *     LOG.warnf("Langfuse refused the call: %s", e.getServerMessage());
     * }
     * }</pre>
     *
     * <p>
     * When no response body was available - a transport failure that never reached the server, or an
     * error response with an empty body - this falls back to {@link #getMessage()} rather than
     * answering an empty string, so the accessor is always worth logging.
     *
     * @return the server's message, or {@link #getMessage()} when no body was supplied
     */
    public String getServerMessage() {
        return Optional.of(this.errorBody.message())
                .filter(Predicate.not(String::isEmpty))
                .orElseGet(this::getMessage);
    }

    /**
     * Returns the body of the failed response as typed data.
     *
     * <p>
     * Never {@code null}: a failure that carried no body answers {@link LangfuseErrorBody#empty()}.
     *
     * @return the error body, never {@code null}
     */
    public LangfuseErrorBody getErrorBody() {
        return errorBody;
    }
}
