package com.estudo.intro_agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
public class ChatRedisMemoryIdController {

    private final ChatClient chatClient;

    public ChatRedisMemoryIdController(ChatClient.Builder chatClientBuilder, RedisChatMemoryRepository  redisChatMemoryRepository) {
        final var memoryAdvisor = MessageChatMemoryAdvisor
                .builder(
                        MessageWindowChatMemory.builder()
                                .chatMemoryRepository(redisChatMemoryRepository)
                                .maxMessages(20)
                                .build()
                )
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(memoryAdvisor, new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/redis-memory-id")
    public String memoryId(@RequestParam String message, @RequestParam String id) {
        return chatClient
                .prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, id))
                .call()
                .content();
    }
}
