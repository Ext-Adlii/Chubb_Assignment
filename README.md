# Chubb APAC Claims Platform

A robust, production-grade full-stack platform for managing motor and property claims across Chubb APAC markets. Built for the Chubb APAC Engineering Take-Home Assessment (Young Graduate Program).

---

## 🏗️ Architecture & Tech Stack

- **Backend Services**: Java 21, Spring Boot 3.2, Spring Data JPA, Hibernate, Flyway
- **Messaging / Event Streaming**: Apache Kafka (KRaft mode)
- **Database**: PostgreSQL 16 (with JSONB outbox payload support)
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Containerization**: Docker Compose

### Microservices Boundary
1. **`claims-service`**: Source of truth for claims. Handles claim submission (with idempotency), status state machine transitions, officer assignment, assessments, info requests, and decisions. Emits domain events reliably using the **Transactional Outbox Pattern**.
2. **`insights-service`**: Analytics projection service. Consumes claims events from Kafka (`claims.events`) to maintain real-time team workload and liability exposure metrics.
3. **`frontend`**: Angular frontend placeholder.

---

## 📊 Core Features & Engineering Highlights

- **Robust State Machine**: Enforces strict business rules for claim lifecycle (`SUBMITTED → UNDER_REVIEW → INFO_REQUESTED → UNDER_REVIEW → SETTLED` or `REJECTED`).
- **Idempotent Claim Creation**: Prevents duplicate claims via `Idempotency-Key` headers.
- **Optimistic Locking**: Uses JPA `@Version` to handle concurrent officer updates safely.
- **Transactional Outbox Pattern**: State changes and audit events (`outbox_events` table with `JSONB` payloads) are written atomically in a single database transaction, ensuring zero message loss before Kafka publishing.
- **Role-Based Access Control**: Secures officer endpoints against claimant role invocations (`403 Forbidden`).
- **Global Error Handling**: Clean exception mapping (`409 Conflict` for state violations, `400 Bad Request` for validation failures).

---

## 🚀 How to Run and Test the Application

### Prerequisites
- Docker & Docker Compose
- Java 21 & Maven (or use the provided Docker Maven runner)

### Step 1: Start Infrastructure (PostgreSQL & Kafka)
```powershell
docker compose up -d
```

### Step 2: Run Automated Tests
To run unit and integration tests (Flyway migrations, JPA validation, state machine rules):
```powershell
docker run --rm --net chubb-claims-platform_default -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/claims_db -v "${PWD}:/app" -w /app/claims-service maven:3.9-eclipse-temurin-21 mvn test
```

### Step 3: Run the Claims Service Locally
To start the live Spring Boot application:
```powershell
docker run --rm -p 8081:8081 -v maven_cache:/root/.m2 --net chubb-claims-platform_default -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/claims_db -v "${PWD}:/app" -w /app/claims-service maven:3.9-eclipse-temurin-21 mvn spring-boot:run
```

Once running, you can access:
- **Interactive Swagger UI**: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **Actuator Health Check**: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

---

## 📂 Deliverables
- **Git Commit History**: Progressive development history.
- **AI Working Journal**: `AI_WORKING_JOURNAL.md` logging AI prompts, acceptances, challenges, and overrides.
