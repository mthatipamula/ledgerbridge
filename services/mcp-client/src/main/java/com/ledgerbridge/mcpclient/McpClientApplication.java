package com.ledgerbridge.mcpclient;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpClientApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }

    @Override
    public void run(String... args) {

        HttpClientStreamableHttpTransport transport =
                HttpClientStreamableHttpTransport
                        .builder("http://localhost:8080/mcp")
                        .build();

        McpSyncClient client =
                McpClient.sync(transport)
                        .build();

        try {
            client.initialize();

            System.out.println("Connected to LedgerBridge MCP server.");

            var toolsResult = client.listTools();

            System.out.println("MCP tools discovered:");

            toolsResult.tools().forEach(tool -> {
                System.out.println("Tool: " + tool.name());
                System.out.println("Description: " + tool.description());
            });

            var result = client.callTool(
                    new io.modelcontextprotocol.spec.McpSchema.CallToolRequest(
                            "getTransaction",
                            java.util.Map.of(
                                    "transactionId",
                                    "9c079d7b-38ef-4fc8-82cc-1e520c3892a7"
                            )
                    )
            );

            System.out.println("MCP getTransaction result:");
            System.out.println(result);

        } finally {
            client.close();
        }
    }
}