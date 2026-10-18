package com.estudo.intro_agent;


import com.estudo.intro_agent.advisors.TokenUsageAuditAdvisor;
import com.estudo.intro_agent.tools.HelpDeskTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/helpdesk")
public class HelpDeskToolChatController {

    private final ChatClient chatClient;

    public HelpDeskToolChatController(ChatClient.Builder chatClientBuilder,
                                      @Value("classpath:/prompts/helpdesk-prompt.st") Resource systemPrompt,
                                      ChatMemory chatMemory,
                                      HelpDeskTools tools) {
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultTools(tools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build())
                .build();
    }

    @GetMapping
    public String ticketChat(
            @RequestParam String user,
            @RequestParam String m,
            @RequestParam String s
    ) {
        return chatClient.prompt(m)
                //.tools() ou assim
                .toolContext(Map.of("username", user))
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, s))
                .call()
                .content();
    }
}
