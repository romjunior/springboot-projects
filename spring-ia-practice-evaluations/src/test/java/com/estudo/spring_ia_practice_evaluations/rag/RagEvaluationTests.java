package com.estudo.spring_ia_practice_evaluations.rag;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest
@Tag("rag-evaluation")
class RagEvaluationTests extends AbstractRagEvaluationTests {

    @TestFactory
    Stream<DynamicTest> evaluateProcedureQuestions() throws IOException {
        return loadCases().map(evaluationCase -> dynamicTest(
                evaluationCase.caseId() + " - " + evaluationCase.expectedOutcome(),
                () -> evaluate(evaluationCase)));
    }

    private void evaluate(ProcedureEvaluationCase evaluationCase) {
        EvaluationSubject subject = callAndValidateRag(evaluationCase);
        EvaluationResponse evaluation = new RelevancyEvaluator(chatClientBuilder).evaluate(new EvaluationRequest(
                evaluationCase.query(), subject.context(), subject.answer()));
        String feedback = normalizeFeedback(evaluation.getFeedback());

        System.out.printf(Locale.ROOT, "RELEVANCY_RESULT|%s|pass=%s|score=%.2f|feedback=%s%n",
                evaluationCase.caseId(), evaluation.isPass(), evaluation.getScore(), feedback);

        assertThat(evaluation.isPass())
                .as("Spring AI RelevancyEvaluator rejected case %s; score=%.2f; feedback=%s; query=%s; answer=%s; retrievedSources=%s",
                        evaluationCase.caseId(), evaluation.getScore(), feedback, evaluationCase.query(), subject.answer(), subject.retrievedSources())
                .isTrue();
    }

    private String normalizeFeedback(String feedback) {
        return feedback == null ? "" : feedback.replaceAll("\\s+", " ").strip();
    }
}
