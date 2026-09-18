package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.ChatPrompt1;
import com.langfuse.api.model.CreateChatPromptRequest;
import com.langfuse.api.model.CreatePromptRequest;
import com.langfuse.api.model.CreateTextPromptRequest;
import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.TextPrompt1;

import io.quarkiverse.langfuse.util.ValidationUtils;

/**
 * Extracts natural-key names from polymorphic prompt models.
 *
 * <p>
 * The prompt counterpart of {@link EvaluatorNames}, and deliberately written the same way: both
 * {@link Prompt} and {@link CreatePromptRequest} are generated {@code oneOf} wrappers whose actual
 * instance is one of two unrelated types, neither of which shares a supertype declaring
 * {@code getName()}.
 */
final class PromptNames {

    private PromptNames() {
    }

    /**
     * Extracts the prompt name from a {@link Prompt}.
     *
     * @param prompt the prompt, may be {@code null}
     * @return the prompt name, or {@code null} if absent or unrecognized
     */
    static String of(Prompt prompt) {
        return Optional.ofNullable(prompt)
                .map(Prompt::getActualInstance)
                .flatMap(instance -> Optional.of(instance)
                        .filter(ChatPrompt1.class::isInstance)
                        .map(ChatPrompt1.class::cast)
                        .map(ChatPrompt1::getName)
                        .or(() -> Optional.of(instance)
                                .filter(TextPrompt1.class::isInstance)
                                .map(TextPrompt1.class::cast)
                                .map(TextPrompt1::getName)))
                .orElse(null);
    }

    /**
     * Extracts the prompt name from a {@link CreatePromptRequest}.
     *
     * @param request the create request, must not be {@code null}
     * @return the prompt name, or {@code null} if unrecognized
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    static String of(CreatePromptRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        return Optional.ofNullable(request.getActualInstance())
                .flatMap(instance -> Optional.of(instance)
                        .filter(CreateChatPromptRequest.class::isInstance)
                        .map(CreateChatPromptRequest.class::cast)
                        .map(CreateChatPromptRequest::getName)
                        .or(() -> Optional.of(instance)
                                .filter(CreateTextPromptRequest.class::isInstance)
                                .map(CreateTextPromptRequest.class::cast)
                                .map(CreateTextPromptRequest::getName)))
                .orElse(null);
    }
}
