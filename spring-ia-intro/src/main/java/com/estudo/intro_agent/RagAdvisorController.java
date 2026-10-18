package com.estudo.intro_agent;

import com.estudo.intro_agent.rag.SensitiveDataDocumentPostProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag-advisor")
public class RagAdvisorController {

    private final ChatClient chatClient;

    private final Resource systemPrompt;

    public RagAdvisorController(ChatClient.Builder chatClientBuilder,
                                VectorStore vectorStore,
                                @Value("classpath:/prompts/system-prompt-rag.st") Resource systemPrompt) {
        this.systemPrompt = systemPrompt;

        SensitiveDataDocumentPostProcessor documentPostProcessor =
                SensitiveDataDocumentPostProcessor.builder().build();

        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor =
                RetrievalAugmentationAdvisor.builder()
                        .queryTransformers(TranslationQueryTransformer.builder()
                                .chatClientBuilder(chatClientBuilder.defaultAdvisors(
                                        new SimpleLoggerAdvisor()
                                ).clone())
                                .targetLanguage("pt-br")
                                .build())
                        .documentRetriever(
                                VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .topK(6)
                                        .similarityThreshold(0.5)
                                        .build()
                        )
                        .documentPostProcessors(documentPostProcessor)
                        .build();

        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        retrievalAugmentationAdvisor
                )
                .build();
    }

    @GetMapping
    public ResponseEntity<String> chat(
            @RequestParam("m") String message
    ) {
        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .call()
                .content();

        return ResponseEntity.ok(answer);
    }
}
