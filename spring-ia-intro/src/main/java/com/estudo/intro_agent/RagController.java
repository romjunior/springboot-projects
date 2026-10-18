package com.estudo.intro_agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    private final Resource prompt;

    private final Resource ragPrompt;

    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    public RagController(ChatClient.Builder chatClientBuilder,
                         VectorStore vectorStore,
                         RedisChatMemoryRepository  redisChatMemoryRepository,
                         @Value("classpath:/prompts/system-prompt-random-data.st") Resource prompt,
                         @Value("classpath:/prompts/system-prompt-rag.st") Resource ragPrompt
    ) {
        this.vectorStore = vectorStore;
        this.prompt = prompt;
        this.ragPrompt = ragPrompt;
        final var memoryAdvisor = MessageChatMemoryAdvisor
                .builder(
                        MessageWindowChatMemory.builder()
                                .chatMemoryRepository(redisChatMemoryRepository)
                                .maxMessages(20)
                                .build()
                )
                .build();
        this.retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .topK(6)
                                .similarityThreshold(0.5)
                                .build()
                )
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(memoryAdvisor, new SimpleLoggerAdvisor(), this.retrievalAugmentationAdvisor)
                .build();
    }

    @GetMapping
    public ResponseEntity<String> rag(
            @RequestParam("id") String id,
            @RequestParam("m") String message
    ) {
        /*final var search = SearchRequest.builder()
                .query(message)
                .topK(6)
                .similarityThreshold(0.5)
                .build();
        final var docs = vectorStore.similaritySearch(search);
        final var texts = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));*/

        final var answer = chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, id))
                .user(message)
                .call()
                .content();
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/advisor")
    public ResponseEntity<String> ragAdvisor(
            @RequestParam("id") String id,
            @RequestParam("m") String message
    ) {
        return Optional.ofNullable(chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, id))
                .user(message)
                .call().content())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
