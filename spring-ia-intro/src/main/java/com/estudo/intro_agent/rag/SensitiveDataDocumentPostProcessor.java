package com.estudo.intro_agent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.util.List;
import java.util.regex.Pattern;

public class SensitiveDataDocumentPostProcessor implements DocumentPostProcessor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?i)(?<![\\w.+-])[\\w.!#$%&'*+/=?^_`{|}~-]+@[\\w-]+(?:\\.[\\w-]+)+(?![\\w.-])"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\d)(?:\\+?55\\s?)?(?:\\(\\d{2}\\)\\s?|\\d{2}\\s)?(?:9\\s?)?\\d{4}[-.\\s]?\\d{4}(?!\\d)|"
                    + "(?<!\\d)\\d{2}9\\d{8}(?!\\d)"
    );

    private SensitiveDataDocumentPostProcessor() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Builder() {
        }

        public SensitiveDataDocumentPostProcessor build() {
            return new SensitiveDataDocumentPostProcessor();
        }
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        return documents.stream()
                .map(this::maskSensitiveData)
                .toList();
    }

    private Document maskSensitiveData(Document document) {
        String text = document.getText();
        if (text == null || text.isBlank()) {
            return document;
        }

        String maskedText = PHONE_PATTERN.matcher(
                EMAIL_PATTERN.matcher(text)
                        .replaceAll("[E-MAIL MASCARADO]")
        ).replaceAll("[TELEFONE MASCARADO]");

        if (maskedText.equals(text)) {
            return document;
        }

        return document.mutate()
                .text(maskedText)
                .build();
    }
}
