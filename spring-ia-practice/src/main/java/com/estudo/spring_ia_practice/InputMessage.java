package com.estudo.spring_ia_practice;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mensagem enviada ao agente CareFlow")
public record InputMessage(
        @Schema(
                description = "Pergunta ou solicitação do paciente",
                example = "Como devo me preparar para uma endoscopia?",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String message
) {
}
