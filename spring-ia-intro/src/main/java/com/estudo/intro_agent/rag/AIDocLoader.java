package com.estudo.intro_agent.rag;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AIDocLoader {

    private static final Logger log = LoggerFactory.getLogger(AIDocLoader.class);

    private final VectorStore vectorStore;

    private final Resource[] resources;

    public AIDocLoader(VectorStore vectorStore,
                       @Value("classpath:/rags/*.txt") Resource[] resources) {
        this.vectorStore = vectorStore;
        this.resources = resources;
    }

    @PostConstruct
    public void loadDocuments() {
        boolean vectorStoreHasDocuments = !vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("verificação de documentos do estacionamento")
                        .topK(1)
                        .similarityThreshold(0.0)
                        .build()
        ).isEmpty();

        if (vectorStoreHasDocuments) {
            log.info("O banco vetorial já possui documentos. Carga dos RAGs ignorada.");
            return;
        }

        List<Document> documents = new ArrayList<>();
        for (Resource resource : resources) {
            documents.addAll(new TikaDocumentReader(resource).get());
        }

        if (documents.isEmpty()) {
            log.warn("Nenhum documento encontrado em classpath:/rags/*.txt.");
            return;
        }

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMaxNumChunks(1000)
                .build();
        List<Document> chunks = splitter.split(documents);
        vectorStore.add(chunks);
        log.info("{} documentos RAG carregados em {} partes.", documents.size(), chunks.size());
    }

}
