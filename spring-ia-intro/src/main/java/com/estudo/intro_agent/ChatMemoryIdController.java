package com.estudo.intro_agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
public class ChatMemoryIdController {

    private final ChatClient chatClient;

    public ChatMemoryIdController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        final var memoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(memoryAdvisor, new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/memory-id")
    public String memoryId(@RequestParam String message, @RequestParam String id) {
        return chatClient
                .prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, id))
                .call()
                .content();
    }
}
