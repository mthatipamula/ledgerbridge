package com.ledgerbridge.mcpclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

@SpringBootTest
@Tag("agent-evaluation")
class AgentEvaluationRunnerTest {

    @Autowired
    private SettlementAssistant settlementAssistant;

    @Autowired
    private AgentToolCallRecorder recorder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void evaluateAgentScenarios() throws Exception {
        EvaluationFile file = objectMapper.readValue(
                new ClassPathResource(
                        "agent-evaluations/scenarios.json").getInputStream(),
                new TypeReference<EvaluationFile>() {});

        for (AgentEvaluationScenario scenario : file.scenarios()) {
            recorder.startRecording();

            try {
                System.out.println("\n=== Scenario: "
                        + scenario.id() + " ===");

                String answer =
                        settlementAssistant.ask(scenario.prompt());

                System.out.println("Expected tool: "
                        + scenario.expectedTool());
                System.out.println("Expected outcome: "
                        + scenario.expectedOutcome());
                System.out.println("Agent answer: " + answer);

                for (var call : recorder.getRecordedCalls()) {
                    System.out.println("Actual tool: " + call.toolName());
                    System.out.println("Arguments: " + call.arguments());
                    System.out.println("Result: " + call.result());
                    System.out.println("Error: " + call.error());
                }
            } finally {
                recorder.stopRecording();
            }
        }
    }

    record EvaluationFile(List<AgentEvaluationScenario> scenarios) {}
}