# AI Working Journal — Chubb Claims Platform

*Running log of AI interaction: prompts, acceptances, challenges, overridings, and reasoning.*

## Entry 1: Initial Scaffolding & Database Migration
- **Date:** 2026-11-21
- **Task:** Set up multi-module Maven project structure, Docker Compose (PostgreSQL 16 + Kafka KRaft), `application.yml`, and Flyway V1 initial schema migration covering claims, claimants, officers, info requests, status history, decisions, idempotency keys, and outbox events with JSONB payloads.
- **What was asked / directed:** Scaffold Spring Boot 3.2 backend with Postgres datasource, validate schema via JPA, set up Flyway migrations with all core entities and queue/claimant indexes.
- **AI Response & Acceptance:** AI provided the Spring Boot configuration, Flyway V1 SQL script with comprehensive table definitions and indexes, and verified compilation and schema application against PostgreSQL using Testcontainers / Docker.
- **Challenges / Overrides:** 
  - *Challenge:* The `Claim` entity defined `claimantId` and `assignedOfficerId` as `String` fields, whereas the initial draft migration used `UUID` with foreign key constraints. 
  - *Override / Adjustment:* Adjusted the migration SQL to use `VARCHAR` for `claimant_id` and `assigned_officer_id` on the `claims` table to match the JPA entity while preserving application-layer referential integrity and supporting cross-module boundaries cleanly.

## Entry 2: Domain Unit Tests & JPA Repositories
- **Date:** 2026-11-21
- **Task:** Implement pure unit tests for `Claim` state machine (`ClaimTest`) and create JPA entity models with corresponding Spring Data repositories.
- **What was asked / directed:** Build comprehensive domain tests covering valid state transition flows (to SETTLED and REJECTED), invalid transitions throwing exceptions, and assessment rules.
- **AI Response & Acceptance:** Created `ClaimTest` with JUnit 5 / AssertJ, implemented entity models matching the Flyway schema, and created Spring Data JPA repositories.
- **Challenges / Overrides:** All 5 tests passed successfully on the first run (`BUILD SUCCESS`).

## Entry 3: Service Layer, Transactional Outbox, REST API, and Insights Service
- **Date:** 2026-11-21
- **Task:** Implement `ClaimService` (idempotency, state machine orchestration, outbox event emission), `OutboxPublisher` (scheduled Kafka publisher), `ClaimController` (REST endpoints with role checks, validation, and idempotency headers), `GlobalExceptionHandler` (mapping state/validation errors to 409/400), and `insights-service` (Kafka event listener projecting workload and exposure metrics).
- **What was asked / directed:** Complete the end-to-end backend functionality satisfying the core assessment requirements for claims processing and insights analytics.
- **AI Response & Acceptance:** Implemented all core backend services, controllers, outbox publisher, and insights service. Verified successful multi-module compilation (`BUILD SUCCESS`).
- **Challenges / Overrides:** Ensured transactional outbox pattern writes audit events and JSONB payloads atomically in the same database transaction as the state change.
