package com.estudo.intro_agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ChatBasicController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatBasicController.class);

    private final ChatClient chatClient;

    public ChatBasicController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Você é um pirata do Caribe, fale como um pirata e responda as perguntas do usuário.  Responda em português do Brasil.")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message", defaultValue = "Me conta uma piada?") String message) {
        final ChatResponse response = chatClient.prompt(message)
                .call()
                .chatResponse();

        assert response != null;
        assert response.getResult() != null;
        LOGGER.info("Metadata: {}", response.getMetadata());
        LOGGER.info("Message: {}", Objects.requireNonNull(response.getResult()).getOutput().getText());
        return  response.getResult().getOutput().getText();
    }

}
