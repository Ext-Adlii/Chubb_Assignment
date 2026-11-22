# AI Working Journal — Chubb Claims Platform

*Running log of AI interaction: prompts, acceptances, challenges, overridings, and reasoning.*

## Primary User Prompt / Assessment Brief
- **User's Request:** 
  > CHUBB APAC ENGINEERING: Take-Home Assessment — Young Graduate Program (Full-stack Developer). Build the motor and property claims platform for Chubb APAC. 
  > - Claimants: report incident, track claim, provide additional info, receive decisions.
  > - Claims staff: pick up incoming claims, review and assess, progress to settlement or rejection, see team workload and performance.
  > - Technology: Backend in Java/Spring Boot or C#/.NET, frontend in Angular, communication via REST/HTTP and Kafka.
  > - Key Architecture & Delivery Requirements: Domain entities and relationships, single vs multiple services, synchronous vs event-driven operations, frontend/backend contract, asynchronous claims processing, workload and liability exposure tracking, Git commit history, working local application, and AI working journal documenting interaction.

---

## Entry 1: Initial Scaffolding & Database Migration
- **Date:** 2026-11-21
- **Task:** Set up multi-module Maven project structure, Docker Compose (PostgreSQL 16 + Kafka KRaft), `application.yml`, and Flyway V1 initial schema migration.
- **User Prompt for this Step:** 
  > Read CLAUDE.md and docs/design.md. Plan first, then implement only this in claims-service:
  > 1. application.yml: Postgres datasource (claims_db, user/password from docker-compose), JPA with ddl-auto=validate, Flyway enabled, Kafka bootstrap servers configurable via env var (default localhost:9092). No Kafka code yet.
  > 2. Flyway migrations (V1) for: claimants, officers, claims (with version column for optimistic locking, unique claim_number), info_requests, status_history, decisions, idempotency_keys (key, claim_id, created_at), outbox_events (id, aggregate_id, type, payload jsonb, created_at, sent_at).
  > 3. Add indexes for the queue query (status, assigned_officer_id) and for claimant lookups.
  > Do not touch the domain classes or add controllers. Show the plan first and run the app against the compose Postgres to prove migrations apply.
- **AI Response & Acceptance:** AI provided the Spring Boot configuration, Flyway V1 SQL script with comprehensive table definitions and indexes, and verified compilation and schema application against PostgreSQL using Testcontainers / Docker.
- **Challenges / Overrides:** 
  - *Challenge:* The `Claim` entity defined `claimantId` and `assignedOfficerId` as `String` fields, whereas the initial draft migration used `UUID` with foreign key constraints. 
  - *Override / Adjustment:* Adjusted the migration SQL to use `VARCHAR` for `claimant_id` and `assigned_officer_id` on the `claims` table to match the JPA entity while preserving application-layer referential integrity and supporting cross-module boundaries cleanly.

## Entry 2: Domain Unit Tests & JPA Repositories
- **Date:** 2026-11-21
- **Task:** Implement pure unit tests for `Claim` state machine (`ClaimTest`) and create JPA entity models with corresponding Spring Data repositories.
- **User Prompt for this Step:** 
  > Domain unit tests and repositories setup.
- **AI Response & Acceptance:** Created `ClaimTest` with JUnit 5 / AssertJ, implemented entity models matching the Flyway schema, and created Spring Data JPA repositories.
- **Challenges / Overrides:** All 5 tests passed successfully on the first run (`BUILD SUCCESS`).

## Entry 3: Service Layer, Transactional Outbox, REST API, and Insights Service
- **Date:** 2026-11-21
- **Task:** Implement `ClaimService`, `OutboxPublisher`, `ClaimController`, `GlobalExceptionHandler`, and `insights-service` with Kafka consumer and workload/exposure analytics.
- **User Prompt for this Step:** 
  > Continue till end step: implement services, outbox publisher, controllers, exception handling, and insights-service.
- **AI Response & Acceptance:** Implemented all core backend services, controllers, outbox publisher, and insights service. Verified successful multi-module compilation (`BUILD SUCCESS`).
- **Challenges / Overrides:** Ensured transactional outbox pattern writes audit events and JSONB payloads atomically in the same database transaction as the state change.
