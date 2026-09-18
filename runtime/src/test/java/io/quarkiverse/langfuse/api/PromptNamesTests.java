package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langfuse.api.model.ChatPrompt1;
import com.langfuse.api.model.CreateChatPromptRequest;
import com.langfuse.api.model.CreateChatPromptType;
import com.langfuse.api.model.CreatePromptRequest;
import com.langfuse.api.model.CreateTextPromptRequest;
import com.langfuse.api.model.CreateTextPromptType;
import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.TextPrompt1;

class PromptNamesTests {

    @Test
    void extractsNameFromTextPrompt() {
        var text = TextPrompt1.builder()
                .name("my-text-prompt")
                .version(1)
                .prompt("hello")
                .labels(List.of("production"))
                .tags(List.of())
                .type(TextPrompt1.TypeEnum.TEXT)
                .build();

        assertThat(PromptNames.of(new Prompt(text))).isEqualTo("my-text-prompt");
    }

    @Test
    void extractsNameFromChatPrompt() {
        var chat = ChatPrompt1.builder()
                .name("my-chat-prompt")
                .version(1)
                .prompt(List.of())
                .labels(List.of("production"))
                .tags(List.of())
                .type(ChatPrompt1.TypeEnum.CHAT)
                .build();

        assertThat(PromptNames.of(new Prompt(chat))).isEqualTo("my-chat-prompt");
    }

    @Test
    void returnsNullWhenPromptOrInstanceIsNull() {
        assertThat(PromptNames.of((Prompt) null)).isNull();
        assertThat(PromptNames.of(new Prompt())).isNull();
    }

    @Test
    void extractsNameFromCreateTextPromptRequest() {
        var text = CreateTextPromptRequest.builder()
                .name("my-create-text-prompt")
                .prompt("hello")
                .type(CreateTextPromptType.TEXT)
                .build();

        assertThat(PromptNames.of(new CreatePromptRequest(text))).isEqualTo("my-create-text-prompt");
    }

    @Test
    void extractsNameFromCreateChatPromptRequest() {
        var chat = CreateChatPromptRequest.builder()
                .name("my-create-chat-prompt")
                .prompt(List.of())
                .type(CreateChatPromptType.CHAT)
                .build();

        assertThat(PromptNames.of(new CreatePromptRequest(chat))).isEqualTo("my-create-chat-prompt");
    }

    @Test
    void throwsOnNullCreateRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> PromptNames.of((CreatePromptRequest) null))
                .withMessage("request must not be null");
    }

    @Test
    void testJsonDeserialization() throws Exception {
        var json = """
                {
                  "name": "prompt-1",
                  "type": "text",
                  "version": 1,
                  "prompt": "hello",
                  "labels": ["production"],
                  "tags": []
                }
                """;
        var prompt = new ObjectMapper().readValue(json, Prompt.class);

        assertThat(PromptNames.of(prompt)).isEqualTo("prompt-1");
    }
}
