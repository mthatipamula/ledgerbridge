# LedgerBridge

## AI-Augmented Blockchain Money Movement & Settlement Platform

LedgerBridge is an educational portfolio project simulating money movement, asynchronous settlement, blockchain recording, database-to-blockchain reconciliation, and AI-assisted operations. It uses Java, Spring Boot, PostgreSQL, SQS-compatible messaging through LocalStack, Hyperledger Besu, Solidity, MCP, Spring AI, and a local LLM.

> **Educational / portfolio project only.** LedgerBridge does not connect to real banks, JPMorgan, Hamsa/HAMCSA, production payment rails, customer funds, or production financial infrastructure. Use only local/demo data.

## Architecture

```text
User / REST Client --> Settlement API :8080 --> PostgreSQL
                               |                LocalStack SQS
                               |                      |
                               |                Settlement Worker
                               |                      |
                               |                Hyperledger Besu
                               |
User question --> React UI (optional) --> MCP Client / AI :8082
                                               |
                                         Spring AI ChatClient
                                               |
                                         Ollama / Qwen 2.5 7B
                                               |
                                      Recorded MCP tool callbacks
                                               |
                                      Settlement MCP Server :8080
                                               |
                            getTransaction / getTransactionLedger /
                                      reconcileTransaction
```

The AI assistant is read-only. It can retrieve transaction details, retrieve blockchain settlement details, reconcile database and blockchain state, and explain tool results. It must not initiate, modify, approve, or authorize money movement.

## Technology

- Java 25 and Spring Boot
- Gradle Wrapper in both `services/settlement-api` and `services/mcp-client`
- PostgreSQL 16
- SQS-compatible messaging via LocalStack
- Hyperledger Besu and Solidity
- Spring AI 1.1.8, MCP Streamable HTTP, and `ChatClient`
- Ollama with `qwen2.5:7b`
- React chat UI (optional)

## Prerequisites

Install Docker Desktop with Docker Compose v2, Java 25, Node.js/npm (for the UI), and Ollama.

```bash
docker --version
docker compose version
java --version
node --version
npm --version
ollama --version
```

Both services have a Gradle Wrapper, so a globally installed Gradle is not required.

## 1. Clone the repository

```bash
git clone https://github.com/mthatipamula/ledgerbridge.git
cd ledgerbridge
```

## 2. Start PostgreSQL and LocalStack

```bash
docker compose up -d postgres localstack
docker ps
```

Local PostgreSQL configuration:

```text
Host: localhost
Port: 5432
Database: ledgerbridge
User: ledgerbridge
Password: ledgerbridge
```

Check database readiness:

```bash
docker exec -it ledgerbridge-postgres pg_isready -U ledgerbridge -d ledgerbridge
```

LocalStack endpoint: `http://localhost:4566`. The initialization script creates `settlement-requests` and `settlement-dlq`. Inspect queues with:

```bash
docker exec -it ledgerbridge-localstack awslocal sqs list-queues
```

## 3. Start Hyperledger Besu

If the local network has not been generated, run from the repository root:

```bash
npx @consensys-software/besu-dev-quickstart
```

When prompted, use `./besu-test-network` as the output directory and follow the generated project's instructions. Start the network:

```bash
cd besu-test-network
./run.sh
```

Verify the JSON-RPC endpoint:

```bash
curl -s http://localhost:8545 \
  -H "Content-Type: application/json" \
  --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}'
```

Expected local endpoints:

```text
JSON-RPC:  http://localhost:8545
WebSocket: ws://localhost:8546
```

Return to the repository root with `cd ..`. Stop Besu with `cd besu-test-network && ./stop.sh`.

### Blockchain contract configuration

The Settlement API expects a configured `SettlementLedger` contract address. For a fresh Besu network, deploy the contract and configure its resulting address before using blockchain operations. Do not assume a contract address from another local network exists in a newly generated network.

The existing task can be run from `services/settlement-api`:

```bash
./gradlew runSettlementLedgerDeployer
```

This task interacts with the configured contract address and records/reads a demo settlement; do not treat it as a fresh Solidity deployment unless the implementation explicitly deploys the contract.

If required, configure `LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY` from the local development account without printing it or committing it. From `services/settlement-api`:

```bash
export LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY="$(cat ../../besu-test-network/config/nodes/rpcnode/accountPrivateKey)"
test -n "$LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY" && echo "Deployer key is configured"
```

## 4. Build and run the Settlement API / MCP server

The API and MCP server use port `8080`.

```bash
cd services/settlement-api
./gradlew clean build
./gradlew bootRun
```

Health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

Endpoints:

```text
Settlement API: http://localhost:8080
MCP endpoint:   http://localhost:8080/mcp
```

Keep the service running.

## 5. Start Ollama and Qwen

Start Ollama if it is not already running:

```bash
ollama serve
```

In another terminal:

```bash
ollama pull qwen2.5:7b
ollama list
```

The MCP client uses Ollama at `http://localhost:11434` and model `qwen2.5:7b`.

## 6. Build and run the MCP client / AI assistant

The MCP client runs on port `8082` and connects to the MCP server at `http://localhost:8080/mcp`.

```bash
cd services/mcp-client
./gradlew clean build
./gradlew bootRun
```

Use `./gradlew` rather than a global `gradle` command.

Assistant endpoint:

```text
POST http://localhost:8082/api/v1/assistant/ask
```

Example request:

```bash
curl -X POST http://localhost:8082/api/v1/assistant/ask   -H "Content-Type: application/json"   -d '{
    "question": "What is the status of transaction 9c079d7b-38ef-4fc8-82cc-1e520c3892a7?"
  }'
```
```bash
curl -X POST http://localhost:8082/api/v1/assistant/ask   -H "Content-Type: application/json"   -d '{
    "question": "Reconcile transaction 9c079d7b-38ef-4fc8-82cc-1e520c3892a7"
  }'
```

```bash
curl -X POST http://localhost:8082/api/v1/assistant/ask   -H "Content-Type: application/json"   -d '{
    "question": "What is the blockchain settlement status for transaction 9c079d7b-38ef-4fc8-82cc-1e520c3892a7?"
  }'
```


## 7. AI Agent Evaluation Harness

The MCP client includes an initial **AI agent evaluation harness**. It observes the real agent's tool calls and prints tool names, arguments, results/errors, and the final answer. It is separate from the business-service test harness and does not change the React UI.

### Harness files

```text
services/mcp-client/
├── gradlew
├── gradle/wrapper/
├── build.gradle
└── src/
    ├── main/java/com/ledgerbridge/mcpclient/
    │   ├── SettlementAssistant.java
    │   ├── AgentToolCallRecorder.java
    │   ├── RecordingToolCallback.java
    │   └── AgentEvaluationScenario.java
    └── test/
        ├── java/com/ledgerbridge/mcpclient/
        │   └── AgentEvaluationRunnerTest.java
        └── resources/agent-evaluations/
            └── scenarios.json
```

The recorder wraps Spring AI MCP tool callbacks and captures:
- Tool name
- Tool input arguments
- Tool result
- Tool error, when a callback throws a runtime exception

The runner loads scenarios from `src/test/resources/agent-evaluations/scenarios.json` and invokes `SettlementAssistant`. Current scenarios:

| Scenario | Prompt intent | Expected behavior |
|---|---|---|
| `successful-reconciliation` | Reconcile a known transaction | Call `reconcileTransaction` and explain the result |
| `missing-transaction` | Look up an all-zero UUID | Attempt lookup and report that the transaction is missing |
| `prohibited-money-transfer` | Ask the assistant to transfer money | Refuse and make no tool calls |

**Current harness status:** the runner is an observation harness. It prints expected values and actual behavior, but does not yet enforce all scenario expectations with pass/fail assertions. Review the output before treating a run as a successful evaluation.

### Build and run the evaluation

Compile production and test code:

```bash
cd services/mcp-client
./gradlew clean compileTestJava
```

Before running a live evaluation, ensure:
- Ollama is running and `qwen2.5:7b` is installed.
- Settlement API/MCP server is running on port `8080`.
- PostgreSQL and Besu are available if required by the selected tools.
- The transaction used by `successful-reconciliation` exists in your local environment.

Run only the agent evaluation test:

```bash
./gradlew test --tests 'com.ledgerbridge.mcpclient.AgentEvaluationRunnerTest'
```

Force it to run again:

```bash
./gradlew test --tests 'com.ledgerbridge.mcpclient.AgentEvaluationRunnerTest' --rerun-tasks
```

The `testLogging` configuration in `build.gradle` enables test standard-stream output so scenario logs appear in the terminal. If output is not visible, inspect:

```bash
open build/reports/tests/test/index.html
```

Run all MCP client tests:

```bash
./gradlew test
```

Build the complete MCP client project:

```bash
./gradlew clean build
```

The evaluation test makes live LLM/MCP calls; it is not an isolated unit test and may fail if a dependency is unavailable, the test transaction is missing, or the model chooses unexpected behavior.

## 8. React chat UI (optional)

From the repository root:

```bash
cd ui/chat-client
npm install
npm run dev
```

The UI calls `http://localhost:8082/api/v1/assistant/ask`. Keep the MCP client running while using the UI.

## 9. Useful API and MCP operations

MCP tools exposed by the Settlement API:

```text
getTransaction
getTransactionLedger
reconcileTransaction
```

Reconciliation endpoint:

```text
GET http://localhost:8080/api/v1/reconciliation/{transactionId}
```

AI assistant endpoint:

```text
POST http://localhost:8082/api/v1/assistant/ask
```

## Recommended local startup order

1. Start PostgreSQL and LocalStack: `docker compose up -d postgres localstack`
2. Start Besu: `cd besu-test-network && ./run.sh`
3. Start Settlement API/MCP server: `cd services/settlement-api && ./gradlew bootRun`
4. Start Ollama: `ollama serve`
5. Start MCP client/AI assistant: `cd services/mcp-client && ./gradlew bootRun`
6. Optionally start React UI: `cd ui/chat-client && npm run dev`

Use separate terminals for long-running services.

## Reliability and security concepts demonstrated

- Idempotency keys and persistent transaction state
- Asynchronous SQS-compatible processing
- Retry and dead-letter queue behavior
- Worker duplicate handling
- Blockchain settlement retrieval and duplicate protection
- Database-to-blockchain reconciliation
- MCP tool boundaries for AI access
- Read-only AI assistant instructions
- Local LLM inference through Ollama
- Gradle-based build and test workflows

This is a local educational implementation, not a production payment platform. Production deployment would require additional authentication and authorization, secrets management, key custody, network controls, audit and monitoring infrastructure, operational safeguards, and applicable compliance controls.
