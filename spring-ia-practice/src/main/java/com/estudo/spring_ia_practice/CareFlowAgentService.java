package com.estudo.spring_ia_practice;

import org.springframework.stereotype.Service;

@Service
public class CareFlowAgentService {

    private final ProcedureAgent procedureAgent;

    public CareFlowAgentService(ProcedureAgent procedureAgent) {
        this.procedureAgent = procedureAgent;
    }

    public String execute(String message) {
        return procedureAgent.chat(message);
    }

    public EvaluationChatResponse executeForEvaluation(String message, String evaluationCode) {
        return procedureAgent.chatForEvaluation(message, evaluationCode);
    }
}
