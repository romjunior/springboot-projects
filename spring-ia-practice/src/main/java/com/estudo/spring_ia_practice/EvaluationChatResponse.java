package com.estudo.spring_ia_practice;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Resposta do agente e contexto recuperado para uma avaliação")
public record EvaluationChatResponse(
        @Schema(description = "Código informado pelo cliente para correlacionar a avaliação", example = "Q001")
        String evaluationCode,
        @Schema(description = "Resposta produzida pelo agente")
        String answer,
        @Schema(description = "Documentos ou chunks recuperados pelo RAG")
        List<RetrievedDocument> retrievedDocuments
) {

    @Schema(description = "Documento recuperado pelo vector store")
    public record RetrievedDocument(
            @Schema(description = "Identificador do documento")
            String id,
            @Schema(description = "Texto do documento ou chunk")
            String text,
            @Schema(description = "Metadados do documento, incluindo source quando disponível")
            Map<String, Object> metadata
    ) {
    }
}
