package com.estudo.intro_agent.config;

import com.estudo.intro_agent.advisors.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//configurações globais que evitam que você precise ficar fazendo isso no chatBuilder
@Configuration
public class ChatClientBuilderCustomizerConfig {

    @Bean
    public ChatClientBuilderCustomizer loggerCustomizer() {
        return builder -> builder.defaultAdvisors(
                new SimpleLoggerAdvisor()
        );
    }

    @Bean
    @ConditionalOnBooleanProperty(value = "custom.audit.token-usage", havingValue = true)
    public ChatClientBuilderCustomizer auditCustomizer() {
        return builder -> builder.defaultAdvisors(
                new TokenUsageAuditAdvisor()
        );
    }

    //ai invés da IA lidar com o erro, dispara o erro na cara do client
    /*@Bean
    public ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        return new DefaultToolExecutionExceptionProcessor(true);
    }*/
}
