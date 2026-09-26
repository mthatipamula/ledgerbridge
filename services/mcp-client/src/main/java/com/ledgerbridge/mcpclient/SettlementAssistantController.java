package com.ledgerbridge.mcpclient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
public class SettlementAssistantController {

    private final SettlementAssistant settlementAssistant;

    public SettlementAssistantController(
            SettlementAssistant settlementAssistant) {

        this.settlementAssistant = settlementAssistant;
    }

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(
            @RequestBody AskRequest request) {

        String response =
                settlementAssistant.ask(request.question());

        return ResponseEntity.ok(
                new AskResponse(response)
        );
    }

    public record AskRequest(String question) {
    }

    public record AskResponse(String answer) {
    }
}