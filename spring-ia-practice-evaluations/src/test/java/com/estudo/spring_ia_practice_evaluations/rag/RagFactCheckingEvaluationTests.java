package com.estudo.spring_ia_practice_evaluations.rag;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest
@Tag("rag-evaluation-fact-checking")
class RagFactCheckingEvaluationTests extends AbstractRagEvaluationTests {

    private static final String FACT_CHECKING_LIMITATION =
            "FactCheckingEvaluator padrão não fornece score nem feedback.";

    private static final List<String> ABSENCE_SIGNALS = List.of(
            "nao consta", "nao contem", "nao encontrei", "nao ha correspondencia segura",
            "nao foi possivel identificar", "nao possuo informacao", "nao temos informacao",
            "nao ha informacao", "nao ha informacoes", "nao temos dados", "nao esta na base");

    private static final List<String> CLARIFICATION_SIGNALS = List.of(
            "pode informar", "poderia informar", "pode detalhar", "poderia detalhar",
            "mais detalhes", "mais informacoes", "esclarecer", "especificar", "qual exame",
            "qual procedimento", "informe", "forneca", "compartilhe", "nos diga");

    private static final Pattern CLINIC_PROCEDURE_AFFIRMATION = Pattern.compile(
            "(?:\\ba clinica\\b|\\bcareflow\\b)\\s+(?:realiza|oferece|disponibiliza|faz)\\b"
                    + "|\\b(?:realizamos|oferecemos|disponibilizamos|fazemos)\\b");

    @TestFactory
    Stream<DynamicTest> factCheckProcedureAnswers() throws IOException {
        return loadCases().map(evaluationCase -> dynamicTest(
                evaluationCase.caseId() + " - " + evaluationCase.expectedOutcome(),
                () -> evaluate(evaluationCase)));
    }

    private void evaluate(ProcedureEvaluationCase evaluationCase) {
        EvaluationSubject subject = callAndValidateRag(evaluationCase);

        if (evaluationCase.expectedOutcome().equals("NO_MATCH")) {
            evaluateAbstention(evaluationCase, subject);
            return;
        }

        EvaluationResponse evaluation = FactCheckingEvaluator.builder(chatClientBuilder).build().evaluate(
                new EvaluationRequest(subject.context(), subject.answer()));

        System.out.printf(Locale.ROOT, "FACT_CHECKING_RESULT|%s|pass=%s|score=unavailable|feedback=%s%n",
                evaluationCase.caseId(), evaluation.isPass(), FACT_CHECKING_LIMITATION);

        assertThat(evaluation.isPass())
                .as("Spring AI FactCheckingEvaluator rejected case %s; score=unavailable; feedback=unavailable; query=%s; answer=%s; retrievedSources=%s",
                        evaluationCase.caseId(), evaluationCase.query(), subject.answer(), subject.retrievedSources())
                .isTrue();
    }

    private void evaluateAbstention(ProcedureEvaluationCase evaluationCase, EvaluationSubject subject) {
        try {
            validateAbstention(evaluationCase, subject);
            logAbstentionResult(evaluationCase.caseId(), true, "Abstenção validada pelo contrato NO_MATCH.");
        }
        catch (AssertionError assertionError) {
            logAbstentionResult(evaluationCase.caseId(), false,
                    "Contrato NO_MATCH não atendido; consulte o detalhe da falha JUnit.");
            throw assertionError;
        }
    }

    private void logAbstentionResult(String caseId, boolean passed, String feedback) {
        System.out.printf(Locale.ROOT, "ABSTENTION_RESULT|%s|pass=%s|score=unavailable|feedback=%s%n",
                caseId, passed, feedback);
    }

    private void validateAbstention(ProcedureEvaluationCase evaluationCase, EvaluationSubject subject) {
        String normalizedAnswer = normalize(subject.answer());

        assertThat(normalizedAnswer)
                .as("NO_MATCH case %s should acknowledge missing information or a missing safe match; answer=%s",
                        evaluationCase.caseId(), subject.answer())
                .containsAnyOf(ABSENCE_SIGNALS.toArray(String[]::new));
        assertThat(normalizedAnswer)
                .as("NO_MATCH case %s should request an additional detail or clarification; answer=%s",
                        evaluationCase.caseId(), subject.answer())
                .containsAnyOf(CLARIFICATION_SIGNALS.toArray(String[]::new));
        assertThat(normalizedAnswer)
                .as("NO_MATCH case %s must not affirm that the clinic performs the requested procedure; answer=%s",
                        evaluationCase.caseId(), subject.answer())
                .matches(answer -> !statesClinicPerformsProcedure(answer));
    }

    private boolean statesClinicPerformsProcedure(String normalizedAnswer) {
        Matcher matcher = CLINIC_PROCEDURE_AFFIRMATION.matcher(normalizedAnswer);
        while (matcher.find()) {
            String precedingText = normalizedAnswer.substring(Math.max(0, matcher.start() - 16), matcher.start());
            if (!precedingText.endsWith("nao ")) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .strip();
    }
}
