package com.ledgerbridge.mcpclient;

public record AgentEvaluationScenario(
        String id,
        String prompt,
        String expectedTool,
        String expectedOutcome) {
}
