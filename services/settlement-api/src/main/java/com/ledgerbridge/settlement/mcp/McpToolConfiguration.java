package com.ledgerbridge.settlement.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfiguration {

    @Bean
    public ToolCallbackProvider settlementToolCallbackProvider(
            SettlementOperations settlementOperations) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(settlementOperations)
                .build();
    }
}