package com.estudo.intro_agent;

import com.estudo.intro_agent.advisors.TokenUsageAuditAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class PromptTemplateController {
    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateController.class);

    private final ChatClient chatClient;

    private Resource promptTemplate;

    public PromptTemplateController(ChatClient.Builder chatClientBuilder,
                                    @Value("classpath:/prompts/prompt-template-intro.st") Resource promptTemplate) {

        final var chatOptions = ChatOptions.builder()
                .maxTokens(500)
                .temperature(0.7);

        this.chatClient = chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(new SimpleLoggerAdvisor(), new TokenUsageAuditAdvisor())
                .build();
        this.promptTemplate = promptTemplate;
    }

    @GetMapping("/prompt-template")
    public String promptTemplate(@RequestParam("message") String message, @RequestParam("customer") String customer) {
        final ChatResponse response = chatClient.prompt(message)
                .system("""
                        Você é um assistente de atendimento ao cliente no qual você ajuda em escrever e-mails de
                        resposta para aumentar a produtividade do time de atendimento ao cliente.
                        """)
                .user( promptUserSpec -> promptUserSpec
                        .text(promptTemplate)
                                .param("customer", customer)
                                .param("message", message))
                .call()
                .chatResponse();

        assert response != null;
        assert response.getResult() != null;
        logger.info("Metadata: {}", response.getMetadata());
        logger.info("Message: {}", Objects.requireNonNull(response.getResult()).getOutput().getText());
        return  response.getResult().getOutput().getText();
    }
}
