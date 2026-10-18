package com.estudo.spring_ia_practice.ingest;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
public class ProceduresReader {

    private final List<Resource> resources;

    public ProceduresReader(@Value("classpath*:knowledge/procedures/*.md") Resource[] resources) {
        this.resources = List.of(resources);
    }

    List<Document> loadProcedures() {
        return this.resources.stream()
                .flatMap(resource -> this.readResource(resource).stream())
                .toList();
    }

    private List<Document> readResource(Resource resource) {
        String source = resource.getFilename() != null
                ? resource.getFilename()
                : resource.getDescription();

        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(false)
                .withIncludeCodeBlock(true)
                .withIncludeBlockquote(true)
                .withAdditionalMetadata("documentType", "procedure")
                .withAdditionalMetadata("source", source)
                .build();

        List<Document> documents = new MarkdownDocumentReader(resource, config).get();

        return IntStream.range(0, documents.size())
                .mapToObj(index -> documents.get(index)
                        .mutate()
                        .id(stableDocumentId(source, index))
                        .build())
                .toList();
    }

    private String stableDocumentId(String source, int index) {
        String documentKey = source + "#" + index;
        return UUID.nameUUIDFromBytes(documentKey.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
