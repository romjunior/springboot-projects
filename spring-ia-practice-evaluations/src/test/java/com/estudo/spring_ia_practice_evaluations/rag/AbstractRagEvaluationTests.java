package com.estudo.spring_ia_practice_evaluations.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractRagEvaluationTests {

    private static final String CASES_RESOURCE = "rag/procedure-retrieval-cases.jsonl";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected RestClient.Builder restClientBuilder;

    @Autowired
    protected ChatClient.Builder chatClientBuilder;

    @Value("${evaluation.base-url}")
    private String evaluationBaseUrl;

    @Value("${evaluation.case-ids:}")
    private String selectedCaseIds;

    protected Stream<ProcedureEvaluationCase> loadCases() throws IOException {
        ClassPathResource resource = new ClassPathResource(CASES_RESOURCE);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            List<ProcedureEvaluationCase> cases = reader.lines()
                    .filter(line -> !line.isBlank())
                    .map(this::readCase)
                    .filter(evaluationCase -> selectedCaseIds.isBlank()
                            || Set.of(selectedCaseIds.split(",")).contains(evaluationCase.caseId()))
                    .toList();
            return cases.stream();
        }
    }

    protected EvaluationSubject callAndValidateRag(ProcedureEvaluationCase evaluationCase) {
        CareFlowEvaluationResponse response = restClientBuilder
                .baseUrl(evaluationBaseUrl)
                .build()
                .post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Evaluation-Code", evaluationCase.caseId())
                .body(Map.of("message", evaluationCase.query()))
                .retrieve()
                .body(CareFlowEvaluationResponse.class);

        assertThat(response).as("CareFlow API response for %s", evaluationCase.caseId()).isNotNull();
        assertThat(response.evaluationCode())
                .as("the API should echo the evaluation code")
                .isEqualTo(evaluationCase.caseId());
        assertThat(response.answer()).as("the agent answer").isNotBlank();

        Set<String> retrievedSources = response.retrievedDocuments().stream()
                .map(document -> document.metadata().get("source"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(java.util.stream.Collectors.toSet());

        if (evaluationCase.expectedOutcome().equals("MATCH")) {
            assertThat(retrievedSources)
                    .as("expected procedure document should be present in retrieved context")
                    .anyMatch(source -> matchesExpectedProcedure(source, evaluationCase.expectedServiceIds()));
        }
        else if (evaluationCase.expectedOutcome().equals("AMBIGUOUS")) {
            assertThat(retrievedSources)
                    .as("at least one plausible procedure should be present for an ambiguous question")
                    .anyMatch(source -> matchesExpectedProcedure(source, evaluationCase.expectedServiceIds()));
        }

        List<Document> context = response.retrievedDocuments().stream()
                .map(document -> new Document(document.id(), document.text(), document.metadata()))
                .toList();
        return new EvaluationSubject(response.answer(), context, retrievedSources);
    }

    private ProcedureEvaluationCase readCase(String jsonLine) {
        try {
            return objectMapper.readValue(jsonLine, ProcedureEvaluationCase.class);
        }
        catch (IOException exception) {
            throw new IllegalArgumentException("Invalid RAG evaluation case: " + jsonLine, exception);
        }
    }

    private boolean matchesExpectedProcedure(String source, List<String> expectedServiceIds) {
        return expectedServiceIds.stream()
                .map(serviceId -> serviceId.replace("PROC-", ""))
                .map(number -> String.format("%02d-", Integer.parseInt(number)))
                .anyMatch(source::startsWith);
    }

    protected record ProcedureEvaluationCase(
            String caseId,
            String query,
            List<String> expectedServiceIds,
            String expectedOutcome,
            String difficulty
    ) {
    }

    private record CareFlowEvaluationResponse(
            String evaluationCode,
            String answer,
            List<RetrievedDocument> retrievedDocuments
    ) {
    }

    private record RetrievedDocument(
            String id,
            String text,
            Map<String, Object> metadata
    ) {
    }

    protected record EvaluationSubject(
            String answer,
            List<Document> context,
            Set<String> retrievedSources
    ) {
    }
}
