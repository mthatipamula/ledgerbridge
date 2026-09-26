# LedgerBridge

## AI-Augmented Blockchain Money Movement & Settlement Platform

LedgerBridge is an educational portfolio project that simulates institutional money movement and blockchain settlement using a modern, event-driven architecture.

It demonstrates how a financial transaction can move from an API request through persistent transaction state, asynchronous settlement processing, a private Ethereum-compatible blockchain, reconciliation, and an AI-assisted operations layer exposed through the Model Context Protocol (MCP).

> **Portfolio / educational project**
>
> LedgerBridge does not connect to real banks, JPMorgan, Hamsa/HAMCSA, production payment networks, customer accounts, or production financial infrastructure. All accounts, transactions, blockchain data, and infrastructure are local/demo resources.

---

## Why I Built LedgerBridge

The project demonstrates practical engineering skills relevant to fintech, payments, blockchain infrastructure, distributed systems, and AI-enabled engineering:

- Java and Spring Boot backend development
- REST API design
- PostgreSQL persistence
- Idempotent money-movement APIs
- Asynchronous event-driven processing
- Amazon SQS-compatible messaging using LocalStack
- Retry and dead-letter queue behavior
- Blockchain integration using Web3j
- Hyperledger Besu private blockchain
- Solidity smart-contract development
- Database-to-blockchain reconciliation
- MCP server and tool design
- Spring AI integration
- Local LLM integration with Ollama and Qwen 2.5
- AI tool calling against live application data
- CI with GitHub Actions
- Separation of synchronous APIs, asynchronous workers, blockchain infrastructure, and AI operations

---

# Architecture

```text
                         ┌─────────────────────────┐
                         │       User / Client      │
                         └────────────┬────────────┘
                                      │
                                      ▼
                         ┌─────────────────────────┐
                         │   Settlement API        │
                         │   Spring Boot / Java    │
                         └────────────┬────────────┘
                                      │
                         ┌────────────┴────────────┐
                         │                         │
                         ▼                         ▼
                ┌──────────────────┐      ┌──────────────────┐
                │   PostgreSQL     │      │  SQS / LocalStack│
                │ Transaction State│      │ Settlement Events │
                └──────────────────┘      └─────────┬────────┘
                                                     │
                                                     ▼
                                            ┌──────────────────┐
                                            │ Settlement Worker │
                                            │   Spring / SQS    │
                                            └─────────┬────────┘
                                                      │
                                                      ▼
                                            ┌──────────────────┐
                                            │ Hyperledger Besu  │
                                            │ Private EVM Chain │
                                            └─────────┬────────┘
                                                      │
                                                      ▼
                                            ┌──────────────────┐
                                            │ SettlementLedger  │
                                            │ Solidity Contract │
                                            └──────────────────┘

                 ┌─────────────────────────────────────────────┐
                 │           AI / MCP Operations Layer         │
                 │                                             │
                 │  Qwen 2.5 7B ← Spring AI ← MCP Client     │
                 │                         │                   │
                 │                         ▼                   │
                 │                 LedgerBridge MCP Server     │
                 │                         │                   │
                 │              ┌──────────┼──────────┐        │
                 │              ▼          ▼          ▼        │
                 │        getTransaction  getLedger  reconcile │
                 └─────────────────────────────────────────────┘
```

---

# Transaction Lifecycle

```text
POST /api/v1/money-movements
             │
             ▼
      Validate request
             │
             ▼
   Check idempotency key
             │
             ▼
   Persist transaction
        status=PENDING
             │
             ▼
      Publish event
             │
             ▼
      SQS settlement queue
             │
             ▼
     Settlement worker
             │
             ▼
       status=PROCESSING
             │
             ▼
    Submit to Besu contract
             │
             ▼
      Blockchain confirms
             │
             ▼
       status=CONFIRMED
             │
             ▼
      Store transaction hash
             │
             ▼
     Reconciliation available
```

The asynchronous API flow separates request handling from blockchain confirmation.

---

# Implemented Components

## 1. Settlement API

Technology:

- Java 25
- Spring Boot 3.5.6
- Gradle 9.7.1
- Spring Web
- Spring Validation
- Spring Actuator

The settlement API accepts money-movement requests and manages the transaction lifecycle.

A transaction contains:

- Transaction ID
- Idempotency key
- Source account
- Destination account
- Amount
- Currency
- Status
- Blockchain transaction hash
- Created timestamp
- Updated timestamp

Example:

```json
{
  "id": "9c079d7b-38ef-4fc8-82cc-1e520c3892a7",
  "idempotencyKey": "postgres-demo-001",
  "sourceAccountId": "BANK-A-001",
  "destinationAccountId": "BANK-B-002",
  "amount": 125.5000,
  "currency": "USD",
  "status": "CONFIRMED",
  "blockchainTransactionHash": "0xb559f2933bbe3c85f4156b74a0606edca9c99a9ceb70a9507e4a036731c2c4f1"
}
```

---

# 2. PostgreSQL Persistence

LedgerBridge supports PostgreSQL persistence.

```text
PostgreSQL
    │
    └── money_movement_transactions
```

The transaction table includes:

```text
id
idempotency_key
source_account_id
destination_account_id
amount
currency
status
blockchain_tx_hash
created_at
updated_at
```

Indexes are maintained for transaction status and creation time.

The repository abstraction also supports an in-memory implementation for simple demonstrations.

---

# 3. Idempotency

Money movement APIs must protect against duplicate requests.

LedgerBridge uses an idempotency key with a database uniqueness constraint.

```text
Request
   │
   ▼
Idempotency Key
   │
   ├── New key ──► Create transaction
   │
   └── Existing key ──► Return existing transaction
```

The database enforces uniqueness on `idempotency_key`.

---

# 4. Asynchronous Settlement Processing

LedgerBridge uses asynchronous settlement processing.

Technology:

- Amazon SQS-compatible messaging
- LocalStack
- Spring-based settlement worker
- Settlement event payloads

Queue:

```text
settlement-requests
```

Dead-letter queue:

```text
settlement-dlq
```

The API persists the transaction and publishes a settlement event. The worker consumes the event and performs blockchain settlement.

This demonstrates separation between:

- API request handling
- transaction persistence
- event delivery
- settlement processing
- blockchain confirmation

---

# 5. Retry and Dead-Letter Queue

The SQS flow includes retry behavior and a dead-letter queue.

```text
Settlement Queue
      │
      ▼
  Worker attempt
      │
      ├── Success ───────► Delete message
      │
      └── Failure
             │
             ▼
       Message remains
             │
             ▼
      Visibility timeout
             │
             ▼
       Retry delivery
             │
             ▼
       Retry limit
             │
             ▼
      Settlement DLQ
```

A message is acknowledged only after the settlement operation succeeds.

This demonstrates at-least-once messaging behavior and failure recovery.

---

# 6. Worker Idempotency

The settlement worker protects against duplicate message delivery.

If a transaction is already `CONFIRMED`, the worker skips duplicate settlement processing rather than submitting another blockchain transaction.

```text
Message 1 ──► Settlement ──► CONFIRMED
Message 1 redelivered
             │
             ▼
        Already CONFIRMED
             │
             ▼
        Skip duplicate
```

---

# 7. Hyperledger Besu

LedgerBridge uses a local Hyperledger Besu private network.

The development network provides:

- Ethereum-compatible JSON-RPC
- WebSocket access
- Multiple validators
- RPC node
- Prometheus
- Grafana
- Chainlens
- Loki / Alloy observability components

Local endpoints used during development include:

```text
JSON-RPC:  http://localhost:8545
WebSocket: ws://localhost:8546
Grafana:   http://localhost:3000
Chainlens: http://localhost:8081
```

The Java application communicates with Besu through Web3j.

---

# 8. Solidity SettlementLedger Contract

LedgerBridge contains a Solidity `SettlementLedger` contract.

The contract stores:

- Transaction ID
- Source account
- Destination account
- Amount
- Currency
- Blockchain timestamp

It prevents duplicate settlement records for the same transaction ID.

Core operations:

```text
recordSettlement(...)
getSettlement(transactionId)
```

The contract validates required fields and rejects duplicate transaction IDs.

---

# 9. Real Blockchain Settlement Demonstration

LedgerBridge has been exercised against the local Besu network.

Example transaction:

```text
Transaction ID:
9c079d7b-38ef-4fc8-82cc-1e520c3892a7

Source:
BANK-A-001

Destination:
BANK-B-002

Amount:
125.50 USD

Database status:
CONFIRMED

Blockchain transaction:
0xb559f2933bbe3c85f4156b74a0606edca9c99a9ceb70a9507e4a036731c2c4f1
```

The corresponding settlement was successfully retrieved from the deployed Solidity contract.

The contract stores monetary amounts as integer values. Therefore `125.50 USD` is represented on-chain as `12550` using two decimal places.

---

# 10. Database-to-Blockchain Reconciliation

LedgerBridge includes a reconciliation service.

Endpoint:

```text
GET /api/v1/reconciliation/{transactionId}
```

The service compares the application-side transaction against the blockchain settlement.

It verifies:

- Transaction ID
- Source account
- Destination account
- Amount
- Currency
- Settlement status

Example:

```json
{
  "transactionId": "9c079d7b-38ef-4fc8-82cc-1e520c3892a7",
  "databaseStatus": "CONFIRMED",
  "blockchainStatus": "CONFIRMED",
  "matched": true,
  "message": "Database transaction matches blockchain settlement"
}
```

This creates a control point between off-chain application state and on-chain settlement state.

---

# 11. MCP Server

LedgerBridge exposes settlement operations through the Model Context Protocol.

Technology:

- Spring AI
- Spring AI MCP Server
- Streamable HTTP MCP transport

MCP endpoint:

```text
http://localhost:8080/mcp
```

Three operational tools are exposed:

### `getTransaction`

Retrieves the database transaction by transaction ID.

### `getTransactionLedger`

Retrieves the corresponding blockchain settlement.

### `reconcileTransaction`

Compares database state against blockchain state.

MCP provides a controlled interface between AI clients and application capabilities.

---

# 12. MCP Tool Discovery and Invocation

The standalone MCP client successfully connects to the LedgerBridge MCP server and discovers:

```text
Tool: reconcileTransaction
Tool: getTransactionLedger
Tool: getTransaction
```

The client can invoke these tools directly.

Example result:

```text
Transaction:
9c079d7b-38ef-4fc8-82cc-1e520c3892a7

Source:
BANK-A-001

Destination:
BANK-B-002

Amount:
125.50 USD

Status:
CONFIRMED
```

---

# 13. AI Settlement Assistant

LedgerBridge adds an AI operations layer on top of MCP.

Technology:

- Spring AI 1.1.8
- Spring AI ChatClient
- Ollama
- Qwen 2.5 7B
- MCP tool callbacks

The LLM does not directly access PostgreSQL or Besu.

Instead:

```text
Qwen 2.5
    │
    ▼
Spring AI ChatClient
    │
    ▼
MCP Tool Callback
    │
    ▼
LedgerBridge MCP Server
    │
    ├── PostgreSQL
    ├── Besu
    └── Reconciliation
```

This keeps the AI layer separated from the transaction infrastructure.

---

# 14. AI Tool Calling

Qwen 2.5 7B was selected because it supports tool calling.

Example request:

```text
Use the getTransactionLedger tool for transaction
9c079d7b-38ef-4fc8-82cc-1e520c3892a7.
Do not answer from general knowledge.
Return the blockchain settlement details from the tool.
```

The model invokes the MCP tool and receives actual blockchain data.

Example tool result:

```text
Source Account: BANK-A-001
Destination Account: BANK-B-002
Amount: 12550 raw on-chain units
Currency: USD
Timestamp: 1790357446
```

The key point is that the LLM uses an application tool to obtain live transaction data rather than generating a transaction answer from general knowledge.

---

# 15. Read-Only AI Operations

The Settlement Assistant is intentionally read-only.

The assistant is instructed:

```text
Never initiate, modify, approve, or authorize money movement.
```

The AI layer can:

- Retrieve transaction information
- Retrieve blockchain settlement information
- Perform reconciliation
- Explain returned results

It does not:

- Create money movements
- Approve payments
- Modify transaction state
- Submit blockchain settlements
- Authorize financial activity

---

# 16. AI + MCP Architecture

```text
                         ┌─────────────────────┐
                         │   User Question     │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Qwen 2.5 7B     │
                         │      via Ollama     │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ Spring AI ChatClient│
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ MCP Tool Callbacks  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                  ┌──────────────────────────────────┐
                  │ LedgerBridge MCP Server          │
                  │                                  │
                  │ getTransaction                   │
                  │ getTransactionLedger             │
                  │ reconcileTransaction             │
                  └───────────────┬──────────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
              PostgreSQL       Besu       Reconciliation
```

---

# 17. REST Operational Tool Endpoints

In addition to MCP, equivalent REST operational endpoints are available:

```text
GET /api/v1/tools/get_transaction/{transactionId}

GET /api/v1/tools/get_transaction_ledger/{transactionId}

GET /api/v1/tools/reconcile_transaction/{transactionId}
```

This allows the capabilities to be demonstrated independently of an AI client.

---

# 18. Configuration and Secrets

Blockchain configuration is externalized.

Example:

```yaml
ledgerbridge:
  blockchain:
    rpc-url: http://localhost:8545
    settlement-contract-address: "..."
```

The blockchain deployer private key is supplied through an environment variable rather than committed to source control:

```text
LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY
```

No private key is stored in the repository.

---

# 19. CI / Build Automation

The repository includes GitHub Actions CI.

The workflow:

- Runs on pushes to `main`
- Runs on pull requests targeting `main`
- Uses Java 25
- Executes the Gradle build

Build command:

```bash
./gradlew clean build
```

This provides a repeatable build check for changes merged into the main branch.

---

# Repository Structure

```text
ledgerbridge/
│
├── README.md
├── docker-compose.yml
├── .gitignore
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── localstack/
│   └── init/
│       └── ready.d/
│           └── create-sqs.sh
│
└── services/
    │
    ├── settlement-api/
    │   ├── build.gradle
    │   ├── settings.gradle
    │   ├── gradlew
    │   └── src/
    │       └── main/
    │           ├── java/
    │           │   └── com/ledgerbridge/settlement/
    │           │       ├── controller/
    │           │       ├── domain/
    │           │       ├── repository/
    │           │       ├── service/
    │           │       ├── event/
    │           │       ├── messaging/
    │           │       ├── reconciliation/
    │           │       ├── mcp/
    │           │       └── blockchain/
    │           │
    │           └── resources/
    │               ├── application.yml
    │               └── schema.sql
    │
    └── mcp-client/
        ├── build.gradle
        └── src/
            └── main/
                ├── java/
                │   └── com/ledgerbridge/mcpclient/
                │       ├── McpClientApplication.java
                │       └── SettlementAssistant.java
                │
                └── resources/
                    └── application.yml
```

---

# Engineering Concepts Demonstrated

## Distributed Systems

- Asynchronous processing
- At-least-once message delivery
- Idempotent consumers
- Retry handling
- Dead-letter queues
- Event-driven processing

## Financial Transaction Processing

- Idempotency keys
- Persistent transaction state
- Explicit lifecycle states
- Transaction audit data
- Reconciliation

## Blockchain

- Private EVM network
- Solidity smart contract
- Blockchain transaction submission
- On-chain settlement records
- Transaction confirmation
- Blockchain/application reconciliation

## AI Engineering

- Local LLM inference
- Spring AI
- Tool calling
- MCP
- AI access to application capabilities
- Read-only operational assistant

## Software Architecture

- Separation of concerns
- API / worker separation
- Repository abstraction
- Externalized configuration
- Service boundaries
- Incremental feature development

---

# What the AI Actually Knows

The AI assistant does not receive the entire database or blockchain.

Instead, it has access to explicitly exposed tools:

```text
getTransaction
getTransactionLedger
reconcileTransaction
```

For example:

```text
User:
"What is the blockchain settlement status for transaction X?"

        ↓

LLM decides a tool is required

        ↓

getTransactionLedger(X)

        ↓

MCP Server

        ↓

SettlementLedgerService

        ↓

Besu smart contract

        ↓

Blockchain result

        ↓

LLM explains result
```

This is the core AI architecture demonstrated by LedgerBridge.

---

# Design Decisions

## Why SQS / LocalStack?

SQS provides a straightforward way to demonstrate asynchronous event processing, retries, visibility timeouts, and dead-letter queues without adding Kafka operational complexity.

LocalStack allows the messaging architecture to be demonstrated locally.

## Why Besu?

Besu provides an Ethereum-compatible private blockchain suitable for demonstrating institutional-style settlement workflows without depending on a public blockchain.

## Why PostgreSQL?

PostgreSQL represents the application-side transaction state while the blockchain provides the on-chain settlement record.

This creates a clear reconciliation boundary:

```text
Off-chain state
      vs.
On-chain state
```

## Why MCP?

MCP provides a clean tool boundary between the AI assistant and application capabilities.

Instead of giving the LLM unrestricted infrastructure access, LedgerBridge exposes specific operations.

## Why Qwen 2.5 7B?

The project uses a local Qwen 2.5 7B model through Ollama because the model supports tool calling and can run locally without sending transaction data to an external LLM service.

---

# What Is Not Included

The project intentionally does not attempt to implement a production payment network.

It does not include:

- Real bank integrations
- Real customer accounts
- Real payment rails
- Production custody
- Production key management
- Regulatory/KYC/AML workflows
- Real financial assets
- Asset tokenization
- Public-chain deployment
- Kafka
- RAG/vector database infrastructure
- Autonomous payment authorization

These are outside the current portfolio scope.

---

# Security Considerations

The project demonstrates several security-oriented practices:

- No private keys committed to source control
- Blockchain credentials supplied through environment variables
- AI assistant is read-only
- Explicit separation between AI operations and transaction mutation
- Database uniqueness constraint for idempotency
- Smart contract validation
- Duplicate settlement protection
- Local/private blockchain for development

A production system would require additional controls such as managed key custody, authorization, mTLS, secrets management, audit controls, regulatory controls, network isolation, and comprehensive security monitoring.

---

# Current Implementation Status

```text
[✓] Spring Boot settlement API
[✓] Java 25 / Gradle build
[✓] PostgreSQL transaction persistence
[✓] Idempotency
[✓] Synchronous transaction flow
[✓] SQS-compatible asynchronous settlement
[✓] LocalStack
[✓] Settlement worker
[✓] Retry behavior
[✓] Dead-letter queue
[✓] Worker duplicate handling
[✓] Hyperledger Besu integration
[✓] Solidity SettlementLedger contract
[✓] Blockchain settlement submission
[✓] Blockchain settlement retrieval
[✓] Database/blockchain reconciliation
[✓] REST operational tools
[✓] MCP server
[✓] MCP Streamable HTTP
[✓] MCP tool discovery
[✓] MCP tool invocation
[✓] Separate MCP client
[✓] Spring AI ChatClient
[✓] Ollama integration
[✓] Qwen 2.5 7B
[✓] LLM tool calling through MCP
[✓] Read-only AI settlement assistant
[✓] GitHub Actions CI
```

---

# Example End-to-End AI Flow

Example question:

```text
Use the getTransactionLedger tool for transaction
9c079d7b-38ef-4fc8-82cc-1e520c3892a7.
Do not answer from general knowledge.
Return the blockchain settlement details from the tool.
```

Architecture:

```text
Qwen 2.5 7B
      │
      │ tool call
      ▼
Spring AI
      │
      ▼
MCP Client
      │
      ▼
LedgerBridge MCP Server
      │
      ▼
getTransactionLedger
      │
      ▼
SettlementLedgerService
      │
      ▼
Web3j
      │
      ▼
Hyperledger Besu
      │
      ▼
SettlementLedger.sol
      │
      ▼
Actual on-chain settlement
```

The LLM then receives the tool result and generates a human-readable response.

---

# Portfolio Value

LedgerBridge combines:

```text
Enterprise API
      +
Persistent transaction state
      +
Asynchronous messaging
      +
Distributed-system reliability
      +
Blockchain settlement
      +
Reconciliation
      +
MCP
      +
LLM tool calling
```

The architecture demonstrates how AI can sit **on top of existing financial-system capabilities** rather than replacing the underlying transaction-processing system.

The transaction system remains deterministic; the AI layer provides a natural-language operational interface over controlled tools.

---

# Future Extensions

Potential future extensions include:

- Production-grade authentication and authorization
- OAuth2 / JWT security
- Role-based access control
- OpenTelemetry tracing
- Prometheus metrics
- Structured audit events
- Transaction history APIs
- Multi-network blockchain support
- Multiple settlement assets
- Approval workflows
- Human-in-the-loop AI operations
- Additional MCP operational tools
- AI-generated reconciliation explanations
- Production secrets management
- Cloud deployment

These are future extensions rather than requirements for the current portfolio implementation.

---

## Summary

LedgerBridge is a portfolio implementation of an **AI-augmented blockchain settlement platform**.

The system demonstrates a complete flow from:

```text
API request
   ↓
Database transaction
   ↓
Asynchronous settlement event
   ↓
SQS worker
   ↓
Private blockchain
   ↓
Smart contract
   ↓
Blockchain confirmation
   ↓
Reconciliation
   ↓
MCP tools
   ↓
Qwen-powered AI assistant
```

The key engineering principle is:

```text
Deterministic transaction processing
                +
Controlled AI-assisted operations
```

The blockchain, database, messaging, and reconciliation layers remain explicit application components, while the LLM interacts with them through narrowly defined MCP tools.