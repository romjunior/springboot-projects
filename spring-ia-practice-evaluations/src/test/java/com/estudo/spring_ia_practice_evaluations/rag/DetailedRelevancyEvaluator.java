package com.estudo.spring_ia_practice_evaluations.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Evaluates RAG answers using a JSON response so the evaluator's rationale is retained
 * in {@link EvaluationResponse#getFeedback()}.
 */
final class DetailedRelevancyEvaluator implements Evaluator {

    private static final PromptTemplate PROMPT_TEMPLATE = new PromptTemplate("""
            Avalie se a resposta atende à pergunta e se todas as afirmações relevantes são
            sustentadas exclusivamente pelo contexto recuperado.

            Retorne YES somente quando a resposta for relevante para a pergunta e estiver
            fundamentada no contexto. Retorne NO quando ela não responder à pergunta, inventar
            informação, contradizer o contexto ou ignorar informação indispensável.

            Responda somente com um objeto JSON válido, sem markdown nem texto adicional.
            O objeto deve ter:
            - decision: a string YES ou NO
            - feedback: uma justificativa obrigatória, concisa e específica, em português

            Pergunta:
            {query}

            Resposta do assistente:
            {response}

            Contexto recuperado:
            {context}
            """);

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    DetailedRelevancyEvaluator(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClientBuilder = Objects.requireNonNull(chatClientBuilder, "chatClientBuilder cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
    }

    @Override
    public EvaluationResponse evaluate(EvaluationRequest evaluationRequest) {
        String context = evaluationRequest.getDataList().stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n---\n\n"));
        String prompt = PROMPT_TEMPLATE.render(Map.of(
                "query", evaluationRequest.getUserText(),
                "response", evaluationRequest.getResponseContent(),
                "context", context));
        String rawResponse = chatClientBuilder.build().prompt().user(prompt).call().content();
        DetailedVerdict verdict = parseVerdict(rawResponse);
        boolean pass = verdict.decision().equals("YES");

        return new EvaluationResponse(pass, pass ? 1.0f : 0.0f, verdict.feedback(),
                Map.of("decision", verdict.decision()));
    }

    private DetailedVerdict parseVerdict(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException("Detailed relevancy evaluator returned an empty response");
        }

        int jsonStart = rawResponse.indexOf('{');
        int jsonEnd = rawResponse.lastIndexOf('}');
        if (jsonStart < 0 || jsonEnd <= jsonStart) {
            throw new IllegalStateException("Detailed relevancy evaluator must return a JSON object: " + rawResponse);
        }

        try {
            JsonNode verdict = objectMapper.readTree(rawResponse.substring(jsonStart, jsonEnd + 1));
            String decision = verdict.path("decision").asText().strip().toUpperCase(Locale.ROOT);
            String feedback = verdict.path("feedback").asText().replaceAll("\\s+", " ").strip();

            if (!Set.of("YES", "NO").contains(decision)) {
                throw new IllegalStateException("Detailed relevancy evaluator returned an invalid decision: " + decision);
            }
            if (feedback.isBlank()) {
                throw new IllegalStateException("Detailed relevancy evaluator returned an empty feedback");
            }
            return new DetailedVerdict(decision, feedback);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException("Detailed relevancy evaluator must return valid JSON: " + rawResponse, exception);
        }
    }

    private record DetailedVerdict(String decision, String feedback) {
    }
}
