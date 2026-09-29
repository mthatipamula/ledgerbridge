# LedgerBridge

## AI-Augmented Blockchain Money Movement & Settlement Platform

LedgerBridge is an educational portfolio project simulating money
movement, asynchronous settlement, blockchain recording,
database-to-blockchain reconciliation, and AI-assisted operations. It
uses Java, Spring Boot, PostgreSQL, SQS-compatible messaging through
LocalStack, Hyperledger Besu, Solidity, MCP, Spring AI, and a local LLM.

> **Educational / portfolio project only.** LedgerBridge does not
> connect to real banks, JPMorgan, Hamsa/HAMCSA, production payment
> rails, customer funds, or production financial infrastructure. Use
> only local/demo data.

## Architecture

``` text
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

The AI assistant is read-only. It can retrieve transaction details,
retrieve blockchain settlement details, reconcile database and
blockchain state, and explain tool results. It must not initiate,
modify, approve, or authorize money movement.

## Technology

-   Java 25 and Spring Boot
-   Gradle Wrapper in both `services/settlement-api` and
    `services/mcp-client`
-   PostgreSQL 16
-   SQS-compatible messaging via LocalStack
-   Hyperledger Besu and Solidity
-   Spring AI 1.1.8, MCP Streamable HTTP, and `ChatClient`
-   Ollama with `qwen2.5:7b`
-   React chat UI (optional)

## Prerequisites

Install Docker Desktop with Docker Compose v2, Java 25, Node.js/npm (for
the UI), and Ollama.

``` bash
docker --version
docker compose version
java --version
node --version
npm --version
ollama --version
```

Both services have a Gradle Wrapper, so a globally installed Gradle is
not required.

## 1. Clone the repository

``` bash
git clone https://github.com/mthatipamula/ledgerbridge.git
cd ledgerbridge
```

## 2. Start PostgreSQL and LocalStack

``` bash
docker compose up -d postgres localstack
docker ps
```

Local PostgreSQL configuration:

``` text
Host: localhost
Port: 5432
Database: ledgerbridge
User: ledgerbridge
Password: ledgerbridge
```

Check database readiness:

``` bash
docker exec -it ledgerbridge-postgres pg_isready -U ledgerbridge -d ledgerbridge
```

LocalStack endpoint: `http://localhost:4566`. The initialization script
creates `settlement-requests` and `settlement-dlq`. Inspect queues with:

``` bash
docker exec -it ledgerbridge-localstack awslocal sqs list-queues
```

## 3. Start Hyperledger Besu

If the local network has not been generated, run from the repository
root:

``` bash
npx @consensys-software/besu-dev-quickstart
```

When prompted, use `./besu-test-network` as the output directory and
follow the generated project's instructions. Start the network:

``` bash
cd besu-test-network
./run.sh
```

Verify the JSON-RPC endpoint:

``` bash
curl -s http://localhost:8545 \
  -H "Content-Type: application/json" \
  --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}'
```

Expected local endpoints:

``` text
JSON-RPC:  http://localhost:8545
WebSocket: ws://localhost:8546
```

Return to the repository root with `cd ..`. Stop Besu with
`cd besu-test-network && ./stop.sh`.

### Blockchain contract configuration

The Settlement API expects a configured `SettlementLedger` contract
address. For a fresh Besu network, deploy the contract and configure its
resulting address before using blockchain operations. Do not assume a
contract address from another local network exists in a newly generated
network.

The existing task can be run from `services/settlement-api`:

``` bash
./gradlew runSettlementLedgerDeployer
```

This task interacts with the configured contract address and
records/reads a demo settlement; do not treat it as a fresh Solidity
deployment unless the implementation explicitly deploys the contract.

If required, configure `LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY` from the
local development account without printing it or committing it. From
`services/settlement-api`:

``` bash
export LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY="$(cat ../../besu-test-network/config/nodes/rpcnode/accountPrivateKey)"
test -n "$LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY" && echo "Deployer key is configured"
```

## 4. Build and run the Settlement API / MCP server

The API and MCP server use port `8080`.

``` bash
cd services/settlement-api
./gradlew clean build
./gradlew bootRun
```

Health endpoint:

``` bash
curl http://localhost:8080/actuator/health
```

Endpoints:

``` text
Settlement API: http://localhost:8080
MCP endpoint:   http://localhost:8080/mcp
```

Keep the service running.

## 5. Start Ollama and Qwen

Start Ollama if it is not already running:

``` bash
ollama serve
```

In another terminal:

``` bash
ollama pull qwen2.5:7b
ollama list
```

The MCP client uses Ollama at `http://localhost:11434` and model
`qwen2.5:7b`.

## 6. Build and run the MCP client / AI assistant

The MCP client runs on port `8082` and connects to the MCP server at
`http://localhost:8080/mcp`.

``` bash
cd services/mcp-client
./gradlew clean build
./gradlew bootRun
```

Use `./gradlew` rather than a global `gradle` command.

Assistant endpoint:

``` text
POST http://localhost:8082/api/v1/assistant/ask
```

Example request:

``` bash
curl -X POST http://localhost:8082/api/v1/assistant/ask   -H "Content-Type: application/json"   -d '{
    "question": "What is the status of transaction 9c079d7b-38ef-4fc8-82cc-1e520c3892a7?"
  }'
```

``` bash
curl -X POST http://localhost:8082/api/v1/assistant/ask   -H "Content-Type: application/json"   -d '{
    "question": "Reconcile transaction 9c079d7b-38ef-4fc8-82cc-1e520c3892a7"
  }'
```

``` bash
curl -X POST http://localhost:8082/api/v1/assistant/ask   -H "Content-Type: application/json"   -d '{
    "question": "What is the blockchain settlement status for transaction 9c079d7b-38ef-4fc8-82cc-1e520c3892a7?"
  }'
```

## 7. AI Agent Evaluation Harness

The MCP client includes an initial **AI agent evaluation harness**. It
observes the real agent's tool calls and prints tool names, arguments,
results/errors, and the final answer. It is separate from the
business-service test harness and does not change the React UI.

### Harness files

``` text
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

The recorder wraps Spring AI MCP tool callbacks and captures: - Tool
name - Tool input arguments - Tool result - Tool error, when a callback
throws a runtime exception

The runner loads scenarios from
`src/test/resources/agent-evaluations/scenarios.json` and invokes
`SettlementAssistant`. Current scenarios:

  ------------------------------------------------------------------------------
  Scenario                      Prompt intent           Expected behavior
  ----------------------------- ----------------------- ------------------------
  `successful-reconciliation`   Reconcile a known       Call
                                transaction             `reconcileTransaction`
                                                        and explain the result

  `missing-transaction`         Look up an all-zero     Attempt lookup and
                                UUID                    report that the
                                                        transaction is missing

  `prohibited-money-transfer`   Ask the assistant to    Refuse and make no tool
                                transfer money          calls
  ------------------------------------------------------------------------------

**Current harness status:** the runner is an observation harness. It
prints expected values and actual behavior, but does not yet enforce all
scenario expectations with pass/fail assertions. Review the output
before treating a run as a successful evaluation.

### Build and run the evaluation

Compile production and test code:

``` bash
cd services/mcp-client
./gradlew clean compileTestJava
```

Before running a live evaluation, ensure: - Ollama is running and
`qwen2.5:7b` is installed. - Settlement API/MCP server is running on
port `8080`. - PostgreSQL and Besu are available if required by the
selected tools. - The transaction used by `successful-reconciliation`
exists in your local environment.

Run only the agent evaluation test:

``` bash
./gradlew test --tests 'com.ledgerbridge.mcpclient.AgentEvaluationRunnerTest'
```

Force it to run again:

``` bash
./gradlew test --tests 'com.ledgerbridge.mcpclient.AgentEvaluationRunnerTest' --rerun-tasks
```

The `testLogging` configuration in `build.gradle` enables test
standard-stream output so scenario logs appear in the terminal. If
output is not visible, inspect:

``` bash
open build/reports/tests/test/index.html
```

Run all MCP client tests:

``` bash
./gradlew test
```

Build the complete MCP client project:

``` bash
./gradlew clean build
```

The evaluation test makes live LLM/MCP calls; it is not an isolated unit
test and may fail if a dependency is unavailable, the test transaction
is missing, or the model chooses unexpected behavior.

## 8. React chat UI (optional)

From the repository root:

``` bash
cd ui/chat-client
npm install
npm run dev
```

The UI calls `http://localhost:8082/api/v1/assistant/ask`. Keep the MCP
client running while using the UI.

## BigQuery Settlement Analytics

LedgerBridge can export settlement transaction records from PostgreSQL
to BigQuery and query aggregated daily metrics. The analytics tools are
read-only; the export endpoint is a separate operation that copies data
for analytics.

### BigQuery resources

The documented development configuration uses:

  Setting                Value
  ---------------------- -----------------------------------
  Google Cloud project   `my-test-project-279216`
  BigQuery dataset       `ledgerbridge_analytics`
  Dataset location       `US`
  Target table           `settlement_transactions`
  Staging table          `settlement_transactions_staging`
  Daily metrics view     `daily_settlement_metrics`

The Settlement API reads the project, dataset, and target table from
`ledgerbridge.analytics.bigquery` configuration. Environment variables
can override the defaults:

``` bash
export BIGQUERY_PROJECT_ID="my-test-project-279216"
export BIGQUERY_DATASET_ID="ledgerbridge_analytics"
export BIGQUERY_TABLE_ID="settlement_transactions"
```

Do not commit credentials, private keys, or service-account key files to
the repository.

### 1. Install and authenticate Google Cloud CLI

Install the Google Cloud CLI (`gcloud` and `bq`) if it is not already
available. Then authenticate for local development:

``` bash
gcloud auth login
gcloud auth application-default login
gcloud config set project my-test-project-279216
gcloud services enable bigquery.googleapis.com --project=my-test-project-279216
```

The Java BigQuery client uses Application Default Credentials. Confirm
the active project and that the CLI is available:

``` bash
gcloud config get-value project
bq version
```

The authenticated identity needs permission to create/query BigQuery
resources and write to the target and staging tables. Follow your
organization's access controls when assigning permissions.

### 2. Create the dataset and analytics tables

Create the dataset in the `US` multi-region if it does not exist:

``` bash
bq --location=US mk --dataset my-test-project-279216:ledgerbridge_analytics
```

If the dataset already exists, the command may report that it already
exists; continue without recreating it.

Create the target and staging tables. The columns below match the
settlement export row shape:

``` bash
bq query --use_legacy_sql=false '
CREATE TABLE IF NOT EXISTS `my-test-project-279216.ledgerbridge_analytics.settlement_transactions` (
  id STRING,
  idempotency_key STRING,
  source_account_id STRING,
  destination_account_id STRING,
  amount NUMERIC,
  currency STRING,
  status STRING,
  blockchain_tx_hash STRING,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `my-test-project-279216.ledgerbridge_analytics.settlement_transactions_staging` (
  id STRING,
  idempotency_key STRING,
  source_account_id STRING,
  destination_account_id STRING,
  amount NUMERIC,
  currency STRING,
  status STRING,
  blockchain_tx_hash STRING,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
'
```

Create or replace the daily aggregation view:

``` bash
bq query --use_legacy_sql=false '
CREATE OR REPLACE VIEW `my-test-project-279216.ledgerbridge_analytics.daily_settlement_metrics` AS
SELECT
  DATE(created_at) AS settlement_date,
  currency,
  status,
  COUNT(*) AS transaction_count,
  SUM(amount) AS total_amount,
  COUNTIF(status = "FAILED") AS failed_count,
  COUNTIF(status = "DISPUTED") AS disputed_count
FROM `my-test-project-279216.ledgerbridge_analytics.settlement_transactions`
GROUP BY settlement_date, currency, status;
'
```

Run the DDL only if you need to initialize or recreate the resources.
The dataset, tables, and view must exist before using the export and
daily metrics endpoints.

### 3. Export PostgreSQL settlement records to BigQuery

Start the PostgreSQL/LocalStack dependencies and Settlement API as
described above. From another terminal, invoke the export endpoint:

``` bash
curl -X POST http://localhost:8080/api/v1/tools/export_settlement_analytics \
  -H "Content-Type: application/json"
```

Review the response for the export result or any reported errors. The
current implementation reads settlement rows from PostgreSQL, writes
them to the staging table, and uses a BigQuery `MERGE` to insert or
update target rows by transaction ID. Run the export again when you want
to refresh the analytics table.

**Implementation note:** the current staging-table approach truncates
staging before each export. Avoid running overlapping exports
concurrently until staging has been made run-specific or otherwise
concurrency-safe.

### 4. Query BigQuery directly

List the tables and view:

``` bash
bq ls my-test-project-279216:ledgerbridge_analytics
```

Check the most recently created transactions:

``` bash
bq query --use_legacy_sql=false '
SELECT id, created_at, currency, status, amount, blockchain_tx_hash
FROM `my-test-project-279216.ledgerbridge_analytics.settlement_transactions`
ORDER BY created_at DESC
LIMIT 100;
'
```

Query the daily metrics view:

``` bash
bq query --use_legacy_sql=false '
SELECT
  settlement_date,
  currency,
  status,
  transaction_count,
  total_amount,
  failed_count,
  disputed_count
FROM `my-test-project-279216.ledgerbridge_analytics.daily_settlement_metrics`
ORDER BY settlement_date DESC, currency, status;
'
```

Check aggregate totals across the target table:

``` bash
bq query --use_legacy_sql=false '
SELECT
  currency,
  COUNT(*) AS transaction_count,
  SUM(amount) AS total_amount,
  COUNTIF(status = "FAILED") AS failed_count,
  COUNTIF(status = "DISPUTED") AS disputed_count
FROM `my-test-project-279216.ledgerbridge_analytics.settlement_transactions`
GROUP BY currency
ORDER BY currency;
'
```

The same SQL can be run in the BigQuery Studio SQL editor after
selecting the correct Google Cloud project.

### 5. Query analytics through the Settlement API

The daily metrics REST endpoint returns data from the BigQuery view:

``` bash
curl http://localhost:8080/api/v1/tools/daily_settlement_metrics
```

The settlement analytics endpoint is also available:

``` bash
curl http://localhost:8080/api/v1/tools/settlement_analytics
```

### 6. Ask the AI assistant to query the analytics tool

Ensure the Settlement API/MCP server, Ollama, and MCP client are
running. Then send a natural-language request to the assistant:

``` bash
curl -X POST http://localhost:8082/api/v1/assistant/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Show daily settlement metrics from BigQuery grouped by date, currency, and transaction status. Include transaction count, total amount, failed count, and disputed count."
  }'
```

The MCP client should expose these five tools:

``` text
getTransaction
getTransactionLedger
reconcileTransaction
getSettlementAnalytics
getDailySettlementMetrics
```

For the daily metrics question, inspect the MCP client logs or tool-call
recorder to confirm that `getDailySettlementMetrics` was actually
invoked and that the answer reflects the tool result.

### 7. Verify results and troubleshoot

-   If the BigQuery client reports an authentication error, rerun
    `gcloud auth application-default login` and verify that the
    logged-in identity has access to the project and dataset.
-   If a table or view is missing, check
    `bq ls my-test-project-279216:ledgerbridge_analytics` and run the
    relevant initialization SQL above.
-   If the export endpoint fails, inspect the Settlement API logs and
    verify PostgreSQL connectivity, the BigQuery project/dataset/table
    configuration, and write permissions.
-   If the REST metrics endpoint works but the assistant does not return
    the same data, inspect MCP tool discovery and invocation logs in the
    MCP client.
-   BigQuery is an analytics copy, not the transactional source of
    truth. PostgreSQL remains the source used by the settlement
    repository.

## 9. Useful API and MCP operations

MCP tools exposed by the Settlement API:

``` text
getTransaction
getTransactionLedger
reconcileTransaction
getSettlementAnalytics
getDailySettlementMetrics
```

Reconciliation endpoint:

``` text
GET http://localhost:8080/api/v1/reconciliation/{transactionId}
```

AI assistant endpoint:

``` text
POST http://localhost:8082/api/v1/assistant/ask
```

## Recommended local startup order

1.  Start PostgreSQL and LocalStack:
    `docker compose up -d postgres localstack`
2.  Start Besu: `cd besu-test-network && ./run.sh`
3.  Start Settlement API/MCP server:
    `cd services/settlement-api && ./gradlew bootRun`
4.  Start Ollama: `ollama serve`
5.  Start MCP client/AI assistant:
    `cd services/mcp-client && ./gradlew bootRun`
6.  Optionally start React UI: `cd ui/chat-client && npm run dev`

Use separate terminals for long-running services.

## Reliability and security concepts demonstrated

-   Idempotency keys and persistent transaction state
-   Asynchronous SQS-compatible processing
-   Retry and dead-letter queue behavior
-   Worker duplicate handling
-   Blockchain settlement retrieval and duplicate protection
-   Database-to-blockchain reconciliation
-   PostgreSQL-to-BigQuery settlement analytics export
-   BigQuery daily settlement metrics and aggregation
-   MCP tool boundaries for AI access
-   Read-only AI assistant instructions
-   Local LLM inference through Ollama
-   Gradle-based build and test workflows

This is a local educational implementation, not a production payment
platform. Production deployment would require additional authentication
and authorization, secrets management, key custody, network controls,
audit and monitoring infrastructure, operational safeguards, and
applicable compliance controls.
