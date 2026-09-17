package io.quarkiverse.langfuse.client.jaxrs;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langfuse.api.LangfuseErrorBody;

/**
 * Turns a raw error response body into a {@link LangfuseErrorBody}.
 *
 * <p>
 * The {@code Content-Type} decides the strategy <em>before</em> anything is parsed. It is the only
 * discriminator that is disjoint by construction and lives outside the body, so a proxy's HTML error
 * page is classified as opaque without a speculative parse-and-catch. Every field-based
 * discriminator was rejected: Langfuse's error shapes are nested rather than disjoint and most
 * endpoints declare no error schema at all.
 *
 * <p>
 * This runs inside an exception mapper, so it must be total: a throw here would replace a clean 404
 * with an unrelated stack trace originating in the mapper itself. Every unparseable, empty, or
 * unexpected body therefore degrades to {@code opaque}/{@code empty}.
 */
final class LangfuseErrorBodies {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private LangfuseErrorBodies() {
    }

    static LangfuseErrorBody parse(MediaType mediaType, String rawBody) {
        return Optional.ofNullable(rawBody)
                .filter(Predicate.not(String::isBlank))
                .map(body -> isJson(mediaType) ? parseJson(body) : LangfuseErrorBody.opaque(body))
                .orElseGet(LangfuseErrorBody::empty);
    }

    /**
     * A {@code null} media type - a response may carry none - answers {@code false}, routing the body
     * to the opaque path.
     *
     * <p>
     * {@code isCompatible} demands equal subtypes, so it covers {@code application/json} and the
     * wildcards but not the structured-suffix types such as {@code application/problem+json}; those
     * are matched separately on the {@code +json} suffix.
     */
    private static boolean isJson(MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)
                || Optional.ofNullable(mediaType)
                        .map(MediaType::getSubtype)
                        .map(subtype -> subtype.toLowerCase(Locale.ROOT))
                        .filter(subtype -> subtype.endsWith("+json"))
                        .isPresent();
    }

    private static LangfuseErrorBody parseJson(String raw) {
        try {
            var root = MAPPER.readTree(raw);
            return root.isObject() ? fromObject(root, raw) : LangfuseErrorBody.opaque(raw);
        } catch (JacksonException e) {
            return LangfuseErrorBody.opaque(raw);
        }
    }

    /**
     * Langfuse names the machine-readable field {@code code} on the endpoints that declare a
     * {@code PublicApiError} and {@code error} elsewhere - {@code {"error":"LangfuseNotFoundError"}} -
     * so both names are consulted, in that order.
     */
    private static LangfuseErrorBody fromObject(JsonNode root, String raw) {
        return LangfuseErrorBody.of(
                textField(root, "message").orElse(raw),
                textField(root, "code")
                        .or(() -> textField(root, "error"))
                        .orElse(null),
                raw);
    }

    private static Optional<String> textField(JsonNode root, String name) {
        return Optional.ofNullable(root.get(name))
                .filter(JsonNode::isValueNode)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .filter(Predicate.not(String::isBlank));
    }
}
