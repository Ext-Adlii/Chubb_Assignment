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
- **Task:** Implement pure unit tests for `Claim` state machine (`ClaimTest`) and create JPA entity models (`IdempotencyKey`, `OutboxEvent`, `InfoRequest`, `StatusHistory`, `Decision`) with corresponding Spring Data repositories.
- **What was asked / directed:** Build comprehensive domain tests covering valid state transition flows (to SETTLED and REJECTED), invalid transitions throwing exceptions, and assessment rules.
- **AI Response & Acceptance:** Created `ClaimTest` with JUnit 5 / AssertJ, implemented entity models matching the Flyway schema (including Hibernate `jsonb` type mapping for `OutboxEvent`), and created Spring Data JPA repositories.
- **Challenges / Overrides:** All 5 tests (`ClaimsServiceApplicationTests` + 4 domain tests) passed successfully on the first run (`BUILD SUCCESS`).
