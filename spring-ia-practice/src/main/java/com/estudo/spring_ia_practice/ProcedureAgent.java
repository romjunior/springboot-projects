package com.estudo.spring_ia_practice;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcedureAgent {

    private final ChatClient chatClient;

    public ProcedureAgent(final ChatClient.Builder chatClientBuilder,
                          @Value("classpath:prompts/procedure-agent.st") final Resource resource,
                          VectorStore vectorStore) {

        final var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .similarityThreshold(0.7)
                .build();

        final var augmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever).build();

        this.chatClient = chatClientBuilder
                .defaultSystem(resource)
                .defaultAdvisors(new SimpleLoggerAdvisor(), augmentationAdvisor)
                .build();
    }

    public String chat(String message) {
        return this.chatClient.prompt(message)
                .call()
                .content();
    }

    public EvaluationChatResponse chatForEvaluation(String message, String evaluationCode) {
        var chatResponse = this.chatClient.prompt(message)
                .call()
                .chatResponse();

        List<Document> retrievedDocuments = extractRetrievedDocuments(
                chatResponse.getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT));

        return new EvaluationChatResponse(
                evaluationCode,
                chatResponse.getResult().getOutput().getText(),
                retrievedDocuments.stream()
                        .map(document -> new EvaluationChatResponse.RetrievedDocument(
                                document.getId(), document.getText(), document.getMetadata()))
                        .toList());
    }

    private List<Document> extractRetrievedDocuments(Object documentContext) {
        if (!(documentContext instanceof List<?> documents)) {
            return List.of();
        }

        return documents.stream()
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .toList();
    }
}
