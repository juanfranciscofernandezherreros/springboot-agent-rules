# Start here

This repository is a ruleset for AI coding agents. It intentionally contains no runnable Spring Boot application; generate each microservice in its own directory or repository.

Before generating or modifying code, the agent must read `AGENTS.md` and every relevant document under `docs/`. Any persistence task must include `docs/database.md`. Any new feature or public API generation must also include `docs/domain-contract.md`.

## Default generation baseline

Unless the user explicitly requests otherwise, generate new Spring Boot services with:

- Java 21;
- Maven Wrapper;
- Spring Boot 4.x;
- Microsoft SQL Server;
- SQL Server-compatible Flyway migrations;
- Docker Compose with the application and SQL Server;
- externalized deployed credentials and connection settings;
- Spotless with Palantir Java Format;
- automated tests appropriate to the feature.

Do not substitute H2, PostgreSQL, MySQL, or another database for SQL Server in a newly generated persistent service unless the user explicitly asks for a different engine.

## Before generating domain code

Establish the feature contract from the user's request, existing API specifications, code, tests, migrations, and documentation. Do not silently invent business fields, states, transitions, validation rules, endpoints, identifiers, uniqueness constraints, financial rules, security behavior, or persistence semantics.

When assumptions are unavoidable, keep them minimal and reversible and document them explicitly as assumptions. Follow `docs/domain-contract.md`.

## Mandatory completion gate

Generation is not complete when files have merely been written. Before committing, pushing, opening a pull request, or reporting completion, run:

```bash
./mvnw --batch-mode --no-transfer-progress spotless:apply
./mvnw --batch-mode --no-transfer-progress spotless:check
./mvnw --batch-mode --no-transfer-progress -Dmaven.test.skip=true clean package
./mvnw --batch-mode --no-transfer-progress verify
./mvnw --batch-mode --no-transfer-progress spotless:check verify
```

Every command must exit with code `0`. If formatting changes source files, rerun the later checks against those formatted files.

When GitHub Actions uses a different Maven Wrapper command, run that exact command too before publishing changes.

### Publication guard for tool-constrained agents

Being able to create or update files in GitHub is not enough to publish a generated Java project safely.

If the current environment cannot execute the target repository's Maven Wrapper, the agent must not push generated or modified Java code directly to the default branch and must not report the project as ready. In particular:

- do not guess what `spotless:apply` would change;
- do not treat hand-formatted Java as equivalent to running Spotless;
- do not rely on GitHub Actions to discover formatting or compilation problems after publication;
- do not weaken or remove `spotless:check` to make CI pass;
- do not claim compilation or test success without command output from the exact revision being published.

The correct behavior when execution is unavailable is to report the blocker rather than publish an unverified default-branch commit. This rule exists specifically to prevent first-run CI failures caused by code that was written but never formatted or compiled.

For a new persistent microservice, continue with the runtime acceptance test required by `docs/testing.md`: validate Compose, build and start the full stack, verify container health, create through the real API, query SQL Server directly, restart containers without deleting volumes, and prove the same row still exists through both API and SQL Server.

If the environment prevents a required verification, state exactly what could not be executed. Never claim that a project is compiled, tested, persistence-verified, CI-ready, or complete without execution evidence for the corresponding check.

## Example: generate a SQL Server project

```text
Read AGENTS.md completely and read every relevant file under docs/
before generating code. Persistence work must follow docs/database.md.
New feature/API work must follow docs/domain-contract.md.

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
- MVC tests when response shape, validation or status mapping is meaningful

Follow AGENTS.md strictly.
Apply formatting first, then run formatting check, production compilation/package,
complete tests, and the exact CI parity command. Fix every failure before publishing.
Then start the complete stack with docker compose, create a record with curl,
verify the row directly in SQL Server, recreate the containers without deleting
the volume, and verify the same record again through the API and SQL Server.
```

## Existing corporate datasource

If the target application already exposes a named datasource such as `spring.datasource.sqlserverdb`, do not replace it with the simple reference-app datasource. Inspect its datasource configuration, persistence unit, entity manager, transaction manager, repository ownership, secret injection, NTLM/TLS/trust-store settings and Hikari configuration, then extend the existing pattern.

If an existing target project already uses a different database engine, preserve it unless the user explicitly requests migration to SQL Server. The SQL Server default applies to new generated persistent services and to requests where no existing datasource determines the engine.

The local Docker configuration is a developer convenience. It must not overwrite corporate authentication or secret-management conventions.
