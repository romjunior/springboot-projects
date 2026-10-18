package com.estudo.intro_agent;


import com.estudo.intro_agent.advisors.TokenUsageAuditAdvisor;
import com.estudo.intro_agent.tools.TimeTools;
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

@RestController
@RequestMapping("/time-tool")
public class TimeToolController {

    private final ChatClient chatClient;

    public TimeToolController(ChatClient.Builder chatClientBuilder,
                              @Value("classpath:/prompts/time-tool-prompt.st") Resource systemPrompt,
                              ChatMemory chatMemory,
                              TimeTools timeTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultTools(timeTools)
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        new TokenUsageAuditAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build())
                .build();
    }

    @GetMapping
    public String timeToolChat(@RequestParam String m, @RequestParam String s) {
        return chatClient.prompt(m)
                //.tools() ou assim
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, s))
                .call()
                .content();
    }
}
