# LedgerBridge

## AI-Augmented Blockchain Money Movement & Settlement Platform

LedgerBridge is a production-oriented fintech platform that simulates institutional money movement and settlement using blockchain rails.

The project is designed to demonstrate production backend engineering, distributed systems, event-driven processing, PostgreSQL, blockchain integration, security, observability, and AI-augmented development using agent workflows and MCP-style tools.

> **Disclaimer:** LedgerBridge is an educational portfolio project. It does not connect to real banking systems, JPMorgan, Hamsa, or production financial infrastructure.

## Architecture

```text
                         Financial Institution
                                  |
                                  | Money Movement Request
                                  v
                    +---------------------------+
                    |       LedgerBridge        |
                    |                           |
                    |     Settlement API        |
                    |     Validation             |
                    |     Idempotency            |
                    |     Risk Controls          |
                    |     Audit                   |
                    +-------------+-------------+
                                  |
                                  | Settlement Event
                                  v
                           +-------------+
                           |     SQS     |
                           +------+------+
                                  |
                                  v
                    +---------------------------+
                    |      Event Worker         |
                    |                           |
                    |  Event Processing         |
                    |  Retry / Recovery         |
                    |  Idempotency              |
                    |  Reconciliation           |
                    +-------------+-------------+
                                  |
                                  | Blockchain Transaction
                                  v
                    +---------------------------+
                    |    Blockchain Network     |
                    |      / Local Besu         |
                    +---------------------------+

                              AI Layer
                                  |
                    +-------------+-------------+
                    |                           |
                    v                           v
              Risk Agent                  RCA Agent
                    |                           |
                    +-------------+-------------+
                                  |
                            MCP-style Tools
                                  |
                    +-------------+-------------+
                    |             |             |
                    v             v             v
               Transactions     Audit      Blockchain
```

## Transaction Lifecycle

```text
CREATE
   |
   v
VALIDATE
   |
   v
PENDING
   |
   v
EVENT PUBLISHED
   |
   v
PROCESSING
   |
   +--------------------+
   |                    |
   v                    v
CONFIRMED             FAILED
   |
   v
SETTLED

If blockchain state and application state diverge:

CONFIRMED / PROCESSING
          |
          v
   RECONCILIATION
          |
          v
       DISPUTED
```

## Core Capabilities

### Money Movement

- Create financial transactions
- Validate transaction requests
- Track transaction lifecycle
- Support currency and amount metadata
- Maintain transaction state in PostgreSQL
- Provide idempotent processing

### Blockchain Settlement

- Submit settlement transactions to a blockchain provider
- Track blockchain transaction hashes
- Process blockchain confirmations
- Handle retryable and terminal failures
- Reconcile application state against blockchain state

### Event-Driven Architecture

The platform uses asynchronous processing so API requests are separated from blockchain settlement work.

```text
Client
  |
  v
Settlement API
  |
  +--> PostgreSQL
  |
  +--> Settlement Event
           |
           v
          SQS
           |
           v
      Event Worker
           |
           v
      Blockchain
```

The API can acknowledge a transaction without waiting for blockchain confirmation, while the worker processes the settlement asynchronously.

## Services

The repository is a polyglot monorepo containing independently deployable services.

```text
ledgerbridge/
|
+-- services/
|   |
|   +-- settlement-api/
|   |      Java / Spring Boot
|   |
|   +-- event-worker/
|          TypeScript / Node.js
|
+-- ai/
|   |
|   +-- agents/
|   +-- tools/
|   +-- mcp/
|
+-- infrastructure/
|
+-- docs/
|
+-- .github/
|      workflows/
|
+-- README.md
```

### Settlement API

The Settlement API is responsible for:

- Transaction creation
- Request validation
- Idempotency
- Transaction persistence
- Publishing settlement events
- Authentication and authorization
- API-level audit information

### Event Worker

The Event Worker is responsible for:

- Consuming settlement events
- Idempotent event handling
- Blockchain transaction submission
- Retry and failure handling
- Processing confirmations
- Reconciliation

## AI-Augmented Operations

AI is treated as an operational and engineering capability rather than simply a chatbot.

Example:

```text
User:
"Why is transaction TX-123 still pending?"

             |
             v

        RCA Agent
             |
       +-----+-----+
       |     |     |
       v     v     v
 Transaction Audit Blockchain
    Tool     Tool    Tool
       |     |     |
       +-----+-----+
             |
             v
       Agent Reasoning
             |
             v
      Root Cause Report
```

Example AI-assisted workflows include:

```text
"Investigate transaction TX-123."

"Why has this settlement been pending?"

"Show the transaction's complete audit trail."

"Check the blockchain transaction status."

"Summarize recent settlement failures."

"Identify transactions requiring reconciliation."
```

## MCP-Style Tooling

AI agents interact with the platform through explicit tools rather than direct database access.

Planned tools include:

- `get_transaction`
- `get_transaction_events`
- `get_blockchain_transaction`
- `get_provider_status`
- `get_audit_events`
- `get_recent_failures`
- `get_reconciliation_status`

This creates a controlled boundary between AI reasoning and production system operations.

## Reliability and Distributed Systems

The platform demonstrates several production-oriented patterns:

- Idempotent event processing
- Asynchronous messaging
- Retry handling
- Optimistic concurrency
- Transaction state machines
- Reconciliation
- Audit trails
- Correlation IDs
- Failure recovery
- Database transactions
- Connection pooling
- Structured logging

## Security

Security is treated as a first-class engineering concern.

Planned capabilities include:

- Authentication
- Role-based authorization
- Input validation
- Secure secret management
- API protection
- Audit logging
- Data protection
- OWASP-oriented secure coding practices
- No hard-coded private keys or production credentials

## Observability

The platform is designed to support production troubleshooting through:

- Structured logs
- Correlation IDs
- Transaction IDs
- Blockchain transaction hashes
- Processing metrics
- Failure metrics
- Reconciliation metrics
- Health checks

## Technology Stack

### Backend

- Java
- Spring Boot
- PostgreSQL
- REST APIs
- Event-driven architecture

### Event Processing

- TypeScript
- Node.js
- Amazon SQS
- Asynchronous workers

### Blockchain

- Hyperledger Besu
- Smart-contract based settlement simulation
- Blockchain transaction tracking

### AI

- Python
- LangGraph
- MCP-style tools
- LLM-based agents
- Multi-step agent workflows

### Frontend

- Next.js
- TypeScript
- React

### Infrastructure

- Docker
- AWS
- GitHub Actions
- Infrastructure as Code

## Development Roadmap

The project is intentionally developed incrementally so each capability can be implemented, tested, reviewed, and demonstrated independently.

### Phase 1 — Transaction Foundation

- Transaction domain model
- PostgreSQL schema
- Settlement API
- Transaction lifecycle
- Idempotency

### Phase 2 — Event-Driven Settlement

- Settlement events
- SQS integration
- Event Worker
- Retry handling
- Dead-letter handling

### Phase 3 — Blockchain

- Local blockchain environment
- Smart contract
- Settlement submission
- Confirmation tracking
- Blockchain failure handling

### Phase 4 — Reconciliation

- Blockchain/application state comparison
- Reconciliation worker
- Dispute detection
- Recovery workflows

### Phase 5 — Security

- Authentication
- Authorization
- Secure secrets
- Audit logging
- API security

### Phase 6 — Observability

- Structured logging
- Metrics
- Distributed correlation
- Health checks
- Operational dashboards

### Phase 7 — AI Platform

- AI agent architecture
- MCP-style tools
- Transaction investigation agent
- Risk analysis agent
- Operations agent
- Root-cause analysis workflows

### Phase 8 — Cloud and CI/CD

- AWS deployment
- Infrastructure as Code
- GitHub Actions
- Automated build and deployment
- Production configuration management

### Phase 9 — Performance and Reliability

- Load testing
- Database indexing
- Connection pool tuning
- Worker concurrency
- Failure injection
- Recovery testing

## Portfolio Objective

LedgerBridge demonstrates how traditional financial-system engineering can be combined with modern AI-native software development.

The project focuses on:

- Production-quality backend engineering
- Distributed systems
- Financial transaction processing
- Blockchain settlement
- PostgreSQL
- Event-driven architecture
- Secure API design
- Reliability engineering
- AI agents
- MCP-style tool orchestration
- AI-assisted development and debugging

The goal is to build a system that can be explained at both the **software architecture level** and the **implementation level**, including the tradeoffs behind reliability, consistency, security, and AI integration.
