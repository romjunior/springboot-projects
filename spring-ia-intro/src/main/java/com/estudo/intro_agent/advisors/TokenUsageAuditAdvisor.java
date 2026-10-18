package com.estudo.intro_agent.advisors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;

public class TokenUsageAuditAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(TokenUsageAuditAdvisor.class);

    private final int order;

    public TokenUsageAuditAdvisor() {
        this.order = 0;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

        if(chatClientResponse.chatResponse().getMetadata() != null) {
            Usage usage = chatClientResponse.chatResponse().getMetadata().getUsage();
            log.info("Token usage - Prompt: {}, Completion: {}, Total: {}",
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens());
        }

        return chatClientResponse;
    }

    @Override
    public String getName() {
        return TokenUsageAuditAdvisor.class.getName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
