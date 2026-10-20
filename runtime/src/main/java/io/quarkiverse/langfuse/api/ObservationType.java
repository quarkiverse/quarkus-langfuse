package io.quarkiverse.langfuse.api;

/**
 * The kind of an observation, as accepted by the {@code type} filter of the Langfuse observations API.
 * <p>
 * The Langfuse OpenAPI specification types that filter as a plain string, so this enum is maintained by hand to give
 * callers a type-safe way of naming the values Langfuse documents.
 */
public enum ObservationType {
    SPAN,
    GENERATION,
    EVENT,
    AGENT,
    TOOL,
    CHAIN,
    RETRIEVER,
    EVALUATOR,
    EMBEDDING,
    GUARDRAIL
}
