package com.estudo.spring_ia_practice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "CareFlow Agent", description = "Atendimento conversacional da clínica CareFlow")
public class CareFlowAgentController {

    private final CareFlowAgentService careFlowAgentService;

    public CareFlowAgentController(CareFlowAgentService careFlowAgentService) {
        this.careFlowAgentService = careFlowAgentService;
    }

    @Operation(
            summary = "Conversar com o agente",
            description = "Envia uma mensagem do paciente e retorna a resposta produzida pelo agente CareFlow."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resposta em texto puro; com X-Evaluation-Code, retorna JSON com a resposta e os documentos recuperados",
                    content = {
                            @Content(
                                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                                    schema = @Schema(implementation = String.class),
                                    examples = @ExampleObject(value = "Para a endoscopia, siga as orientações de preparo fornecidas pela clínica.")
                            ),
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = EvaluationChatResponse.class)
                            )
                    }
            ),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "500", description = "Falha ao processar a mensagem", content = @Content)
    })
    @PostMapping(
            value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> execute(
            @RequestBody InputMessage message,
            @Parameter(description = "Código opcional para identificar uma execução de avaliação", example = "Q001")
            @RequestHeader(name = "X-Evaluation-Code", required = false) String evaluationCode) {
        if (evaluationCode != null && !evaluationCode.isBlank()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(this.careFlowAgentService.executeForEvaluation(message.message(), evaluationCode));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(this.careFlowAgentService.execute(message.message()));
    }
}
