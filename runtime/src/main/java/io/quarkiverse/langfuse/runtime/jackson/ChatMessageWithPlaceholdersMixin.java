package io.quarkiverse.langfuse.runtime.jackson;

import com.langfuse.api.model.ChatMessage;
import com.langfuse.api.model.ChatMessageWithPlaceholders;
import com.langfuse.api.model.PlaceholderMessage;

import io.quarkus.jackson.JacksonMixin;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Fixes polymorphic deserialization of {@link ChatMessageWithPlaceholders}.
 *
 * <p>
 * The generated deserializer tries both {@link ChatMessage} and {@link PlaceholderMessage}.
 * With {@code FAIL_ON_UNKNOWN_PROPERTIES=false}, both always succeed, so it cannot tell them
 * apart and keeps whichever matched last. This MixIn replaces the generated deserializer
 * with one that discriminates based on the {@code role} field (required in {@link ChatMessage}
 * but absent from {@link PlaceholderMessage}).
 */
@JacksonMixin(ChatMessageWithPlaceholders.class)
@JsonDeserialize(using = ChatMessageWithPlaceholdersMixin.ChatMessageWithPlaceholdersDeserializer.class)
public interface ChatMessageWithPlaceholdersMixin {

    class ChatMessageWithPlaceholdersDeserializer extends StdDeserializer<ChatMessageWithPlaceholders> {

        ChatMessageWithPlaceholdersDeserializer() {
            super(ChatMessageWithPlaceholders.class);
        }

        @Override
        public ChatMessageWithPlaceholders deserialize(JsonParser jp, DeserializationContext ctxt) {
            JsonNode tree = ctxt.readTree(jp);

            if (tree.has("role")) {
                return new ChatMessageWithPlaceholders(ctxt.readTreeAsValue(tree, ChatMessage.class));
            }

            return new ChatMessageWithPlaceholders(ctxt.readTreeAsValue(tree, PlaceholderMessage.class));
        }
    }
}
