# Start here

This repository is a ruleset for AI coding agents. It intentionally contains no runnable Spring Boot application; generate each microservice in its own directory or repository.

Before generating or modifying code, the agent must read `AGENTS.md` and every relevant document under `docs/`. Any persistence task must include `docs/database.md`.

## Default generation baseline

Unless the user explicitly requests otherwise, generate new Spring Boot services with:

- Java 21;
- Maven Wrapper;
- Microsoft SQL Server;
- SQL Server-compatible Flyway migrations;
- Docker Compose with the application and SQL Server;
- externalized deployed credentials and connection settings.

Do not substitute H2, PostgreSQL, MySQL, or another database for SQL Server in a newly generated persistent service unless the user explicitly asks for a different engine.

## Example: generate a SQL Server project

```text
Read AGENTS.md completely and read every relevant file under docs/
before generating code. Persistence work must follow docs/database.md.

Create a complete Spring Boot application from scratch with:
- Java 21
- Spring Boot 4.x
- Maven Wrapper
- Microsoft SQL Server
- Spring Data JPA
- Lombok
- Bean Validation
- Flyway
- JUnit 6
- Mockito
- AssertJ
- Spotless with Palantir Java Format

Project:
- name: orders-api
- group: com.acme
- artifact: orders-api
- base package: com.acme.orders

Database:
- engine: SQL Server
- local development: Docker Compose SQL Server
- local database: orders
- deployed connection values: externalized; never hardcode production secrets
- Hibernate schema mode in deployed environments: validate
- migrations: SQL Server-compatible Flyway migrations

Initial feature: Order CRUD

Fields:
- id: Long, generated primary key
- customerReference: String, required, max 100
- status: enum, required
- totalAmount: BigDecimal, required, positive
- createdAt: Instant, generated on create

Operations:
- POST /orders
- GET /orders/{id}
- GET /orders/search with pagination and filters by customerReference and status
- PATCH /orders/{id}
- DELETE /orders/{id}

Generate everything required to build and run the application, including:
- pom.xml
- Maven Wrapper
- main Spring Boot application class
- application.yml
- production-style multi-stage Dockerfile and .dockerignore
- Docker Compose for both the application and SQL Server
- health checks, health-aware startup ordering, and a named SQL Server volume
- SQL Server JDBC and Flyway support
- SQL Server-compatible Flyway migrations
- feature packages and layers
- global exception handling
- unit tests
- MVC tests

Follow AGENTS.md strictly.
Run formatting and the complete test suite when finished and fix any failures.
Then start the complete stack with docker compose, create a record with curl,
verify the row directly in SQL Server, recreate the containers without deleting
the volume, and verify the same record again through the API and SQL Server.
```

## Existing corporate datasource

If the target application already exposes a named datasource such as `spring.datasource.sqlserverdb`, do not replace it with the simple reference-app datasource. Inspect its datasource configuration, persistence unit, entity manager, transaction manager, repository ownership, secret injection, NTLM/TLS/trust-store settings and Hikari configuration, then extend the existing pattern.

If an existing target project already uses a different database engine, preserve it unless the user explicitly requests migration to SQL Server. The SQL Server default applies to new generated persistent services and to requests where no existing datasource determines the engine.

The local Docker configuration is a developer convenience. It must not overwrite corporate authentication or secret-management conventions.
