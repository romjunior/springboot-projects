package com.estudo.spring_ia_practice_evaluations.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest
@Tag("rag-evaluation-detailed")
class RagDetailedEvaluationTests extends AbstractRagEvaluationTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestFactory
    Stream<DynamicTest> evaluateProcedureQuestionsWithFeedback() throws IOException {
        return loadCases().map(evaluationCase -> dynamicTest(
                evaluationCase.caseId() + " - " + evaluationCase.expectedOutcome(),
                () -> evaluate(evaluationCase)));
    }

    private void evaluate(ProcedureEvaluationCase evaluationCase) {
        EvaluationSubject subject = callAndValidateRag(evaluationCase);
        EvaluationResponse evaluation = new DetailedRelevancyEvaluator(chatClientBuilder, objectMapper).evaluate(
                new EvaluationRequest(evaluationCase.query(), subject.context(), subject.answer()));

        System.out.printf(Locale.ROOT, "DETAILED_RELEVANCY_RESULT|%s|pass=%s|score=%.2f|feedback=%s%n",
                evaluationCase.caseId(), evaluation.isPass(), evaluation.getScore(), evaluation.getFeedback());

        assertThat(evaluation.getFeedback())
                .as("Detailed evaluator should explain its judgement for case %s", evaluationCase.caseId())
                .isNotBlank();
        assertThat(evaluation.isPass())
                .as("DetailedRelevancyEvaluator rejected case %s; score=%.2f; feedback=%s; query=%s; answer=%s; retrievedSources=%s",
                        evaluationCase.caseId(), evaluation.getScore(), evaluation.getFeedback(), evaluationCase.query(), subject.answer(), subject.retrievedSources())
                .isTrue();
    }
}
