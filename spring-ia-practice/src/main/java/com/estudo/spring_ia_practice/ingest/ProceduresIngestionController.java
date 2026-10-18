package com.estudo.spring_ia_practice.ingest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ingestion")
@Tag(name = "Ingestion", description = "Ingestão da base de conhecimento no vector store")
public class ProceduresIngestionController {

    private final ProceduresIngestion proceduresIngestion;

    public ProceduresIngestionController(ProceduresIngestion proceduresIngestion) {
        this.proceduresIngestion = proceduresIngestion;
    }

    @Operation(
            summary = "Ingerir procedimentos",
            description = "Lê os documentos Markdown de procedimentos, gera os embeddings e os armazena no vector store."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingestão concluída"),
            @ApiResponse(responseCode = "500", description = "Falha durante a ingestão")
    })
    @PostMapping("/procedures")
    public ResponseEntity<IngestionResponse> ingestProcedures() {
        int documentCount = this.proceduresIngestion.ingest();
        return ResponseEntity.ok(new IngestionResponse(documentCount));
    }

    public record IngestionResponse(int documentsIngested) {
    }
}
