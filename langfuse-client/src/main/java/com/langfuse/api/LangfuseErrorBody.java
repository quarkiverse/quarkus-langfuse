package com.langfuse.api;

import java.io.Serializable;
import java.util.Optional;

/**
 * The body of a failed Langfuse API response, as typed data.
 *
 * <p>
 * Langfuse answers an error with a response body that usually, but not always, contains a
 * human-readable explanation. This type carries that body so a caller can read the server's own
 * words without parsing JSON out of {@link LangfuseApiException#getMessage()}:
 *
 * <pre>{@code
 * try {
 *     datasets.getById("missing");
 * } catch (LangfuseApiException e) {
 *     System.out.println(e.getErrorBody().message());
 * }
 * }</pre>
 *
 * <p>
 * There are two depths of access. {@link #message()} is the common path and is always populated
 * with the most useful text available. {@link #rawBody()} is the escape hatch, holding the response
 * body exactly as it arrived, for the cases the typed view does not cover.
 *
 * <p>
 * Three factories model the three situations a client actually encounters:
 *
 * <ul>
 * <li>{@link #of(String, String, String)} — the body was understood, so its fields are available
 * separately.</li>
 * <li>{@link #opaque(String)} — a body arrived but could not be understood, such as an HTML error
 * page from an intervening proxy. {@link #message()} answers the raw text, so it stays useful.</li>
 * <li>{@link #empty()} — no response body existed at all, which is the case for a transport failure
 * that never reached the server.</li>
 * </ul>
 *
 * <p>
 * Neither {@link #message()} nor {@link #rawBody()} ever answers {@code null}; absence is expressed
 * as an empty string, and only {@link #code()} is genuinely optional.
 *
 * @see LangfuseApiException
 */
public sealed interface LangfuseErrorBody extends Serializable permits DefaultLangfuseErrorBody {

    /**
     * Creates an error body whose contents were understood.
     *
     * @param message the server's explanation, {@code null} is normalized to an empty string
     * @param code the machine-readable error code, {@code null} when the server did not send one -
     *        most Langfuse endpoints do not
     * @param rawBody the response body exactly as it arrived, {@code null} is normalized to an empty
     *        string
     * @return the error body
     */
    static LangfuseErrorBody of(String message, String code, String rawBody) {
        return new DefaultLangfuseErrorBody(message, code, rawBody);
    }

    /**
     * Creates an error body that could not be understood, so it is carried verbatim.
     *
     * <p>
     * {@link #message()} answers {@code rawBody} rather than an empty string: unparseable text is
     * still far more informative to whoever reads the log than nothing at all.
     *
     * @param rawBody the response body exactly as it arrived, {@code null} is normalized to an empty
     *        string
     * @return the error body, with an empty {@link #code()}
     */
    static LangfuseErrorBody opaque(String rawBody) {
        return new DefaultLangfuseErrorBody(rawBody, null, rawBody);
    }

    /**
     * Creates an error body standing for a response that carried no body at all.
     *
     * <p>
     * This is what a transport-level failure produces, where no HTTP response was ever received.
     *
     * @return an error body whose {@link #message()} and {@link #rawBody()} are empty strings and
     *         whose {@link #code()} is empty
     */
    static LangfuseErrorBody empty() {
        return new DefaultLangfuseErrorBody("", null, "");
    }

    /**
     * The server's explanation of the failure.
     *
     * <p>
     * For a body that was understood this is the explanation the server sent. For an
     * {@linkplain #opaque(String) opaque} body it is the whole raw body. For an {@linkplain #empty()
     * empty} body it is an empty string.
     *
     * @return the message, never {@code null} but possibly empty
     */
    String message();

    /**
     * The machine-readable error code, when the server sent one.
     *
     * <p>
     * Only a few Langfuse endpoints declare a code in their error schema, so this is empty far more
     * often than not. Treat its presence as a bonus rather than something to branch the happy path
     * on.
     *
     * @return the code, or {@link Optional#empty()} when the server sent none
     */
    Optional<String> code();

    /**
     * The response body exactly as it arrived, before any interpretation.
     *
     * <p>
     * This is the escape hatch for a body shape this type does not model - parse it yourself if you
     * need a field that {@link #message()} and {@link #code()} do not expose.
     *
     * @return the raw body, never {@code null} but empty when the response carried no body
     */
    String rawBody();
}
