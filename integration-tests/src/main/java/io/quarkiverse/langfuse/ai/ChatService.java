package io.quarkiverse.langfuse.ai;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
@ApplicationScoped
public interface ChatService {
    @SystemMessage("You are a helpful assistant.")
    String chat(@UserMessage String prompt);
}
