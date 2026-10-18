package com.estudo.intro_agent;

import com.estudo.intro_agent.advisors.TokenUsageAuditAdvisor;
import com.estudo.intro_agent.model.CountryCities;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/structured-output")
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor(), new TokenUsageAuditAdvisor())
                .build();
    }

    @GetMapping("/call")
    public CountryCities call(@RequestParam("country") String country) {
        return chatClient.prompt()

                .user(promptUserSpec -> promptUserSpec
                        .text("""
                        Você é um professor de geografia e o seu trabalho é trazer a lista de todas as cidades do pais {country}.
                        Responda apenas com a lista de cidades, sem nenhuma explicação ou texto adicional.
                        """)
                        .param("country", country))
                .call()
                .entity(CountryCities.class);
    }

    @GetMapping("/call-list")
    public List<String> callList(@RequestParam("country") String country) {
        return chatClient.prompt()

                .user(promptUserSpec -> promptUserSpec
                        .text("""
                        Você é um professor de geografia e o seu trabalho é trazer a lista de todas as cidades do pais {country}.
                        Responda apenas com a lista de cidades, sem nenhuma explicação ou texto adicional.
                        """)
                        .param("country", country))
                .call()
                .entity(new ListOutputConverter());
    }

    @GetMapping("/call-map")
    public Map<String, Object> callMap(@RequestParam("country") String country) {
        return chatClient.prompt()

                .user(promptUserSpec -> promptUserSpec
                        .text("""
                        Você é um professor de geografia e o seu trabalho é trazer a lista de todas as cidades do pais {country}.
                        Responda apenas com a lista de cidades, sem nenhuma explicação ou texto adicional.
                        """)
                        .param("country", country))
                .call()
                .entity(new MapOutputConverter());
    }

    @GetMapping("/call-bean-list")
    public List<CountryCities> callBeanList(@RequestParam("country") String country) {
        return chatClient.prompt()

                .user(promptUserSpec -> promptUserSpec
                        .text("""
                        Você é um professor de geografia e o seu trabalho é trazer a lista de todas as cidades dos paises {country}.
                        Responda apenas com a lista de cidades, sem nenhuma explicação ou texto adicional.
                        """)
                        .param("country", country))
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
