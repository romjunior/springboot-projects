package com.estudo.intro_agent;

import com.estudo.intro_agent.advisors.TokenUsageAuditAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class StreamingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingController.class);

    private final ChatClient chatClient;

    public StreamingController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor(), new TokenUsageAuditAdvisor())
                .build();
    }

    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam("message") String message) {
        return chatClient.prompt().user(message).stream().content()
                .doOnNext(chunk -> LOGGER.info("Received chunk: {}", chunk))
                .doOnError(error -> LOGGER.error("Error in stream: ", error));
    }
}
