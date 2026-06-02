package io.quarkiverse.langfuse.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.langfuse.ai.ChatService;

@Path("/chat")
public class ChatResource {
    private final ChatService chatService;

    public ChatResource(ChatService chatService) {
        this.chatService = chatService;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String chat(@QueryParam("message") String message) {
        return this.chatService.chat(message);
    }
}
