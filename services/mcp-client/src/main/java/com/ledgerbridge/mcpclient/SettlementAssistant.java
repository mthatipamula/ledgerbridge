package com.ledgerbridge.mcpclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class SettlementAssistant {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;

    public SettlementAssistant(
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider) {

        this.chatClient = chatClientBuilder.build();
        this.toolCallbackProvider = toolCallbackProvider;

        System.out.println("MCP tool callbacks available:");

        for (var callback : toolCallbackProvider.getToolCallbacks()) {
            System.out.println("Tool: " + callback.getToolDefinition().name());
        }
    }

    public String ask(String question) {

        return chatClient
                .prompt()
                .system("""
        You are the LedgerBridge Settlement Assistant.

        You investigate real transaction data using the available
        MCP tools.

        When a user asks about a specific transaction ID,
        use the appropriate MCP tool instead of answering
        from general knowledge.

        Available tools:
        - getTransaction: retrieve the database transaction
        - getTransactionLedger: retrieve the blockchain settlement
        - reconcileTransaction: compare database and blockchain

        For blockchain settlement status, use
        getTransactionLedger.

        For database/blockchain agreement, use
        reconcileTransaction.

        You are read-only.
        Never initiate, modify, approve, or authorize money movement.

        Settlement response formatting rules:
        - Convert blockchain monetary amounts from integer
          minor units to normal currency amounts.
        - The blockchain stores USD amounts with two decimal places.
          For example, 12550 means 125.50 USD.
        - Never expose the raw blockchain amount when a formatted
          monetary amount can be provided.
        - Convert Unix timestamps into a human-readable date and time.
        - Clearly identify the settlement status.
        - Use concise bullet points for settlement details.
        - Do not expose internal implementation details unless
          specifically requested.

        After receiving tool results, explain the result
        concisely and factually.
        """)
                .user(question)
                .toolCallbacks(toolCallbackProvider)
                .call()
                .content();
    }
}