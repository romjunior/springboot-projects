package com.estudo.intro_agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sm-cache")
public class SemanticCacheController {

    private final ChatClient chatClient;

    private final Resource systemPrompt;

    public SemanticCacheController(ChatClient.Builder chatClientBuilder,
                                   @Value("classpath:/prompts/sm-cache.st") Resource systemPrompt,
                                   SemanticCache smCache) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        SemanticCacheAdvisor.builder().cache(smCache).build()
                )
                .build();
        this.systemPrompt = systemPrompt;

    }

    @GetMapping
    public String chat(@RequestParam("message") String message) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .call()
                .content();
    }
}
