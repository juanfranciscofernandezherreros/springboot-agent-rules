# Spring Boot Agent Rules

A practical, opinionated ruleset for AI coding agents that generate and maintain Spring Boot backends with consistent architecture, naming, testing, database handling, and code style.

The repository contains agent instructions and engineering standards in [`AGENTS.md`](AGENTS.md) and [`docs/`](docs/). It intentionally contains no generated microservice, build output, or database runtime.

The goal is to give an AI coding agent enough explicit context to produce code that looks like it belongs to the same codebase every time, including database-specific behavior.

## Rules

`AGENTS.md` is the root instruction file. Detailed standards live under `docs/`:

| Document | Purpose |
| --- | --- |
| [`java-style.md`](docs/java-style.md) | Java style and formatting |
| [`annotations.md`](docs/annotations.md) | Annotation usage and placement |
| [`layered-architecture.md`](docs/layered-architecture.md) | Feature-oriented layered architecture |
| [`controllers.md`](docs/controllers.md) | REST controller conventions |
| [`mappers.md`](docs/mappers.md) | DTO, model, and entity mappings |
| [`exceptions.md`](docs/exceptions.md) | Application/API error handling |
| [`testing.md`](docs/testing.md) | Unit and integration testing conventions |
| [`logging.md`](docs/logging.md) | Logging conventions |
| [`database.md`](docs/database.md) | SQL Server, Docker, Flyway, datasources, secrets, and multi-datasource rules |

Persistence work must read `docs/database.md` in addition to the architectural rules.

## Architecture

Features are organized by domain and split into focused layers:

```text
com/<company>/<app>/<feature>/
├── controller/
├── service/
├── repository/
├── model/
├── entity/
├── dto/
└── mapper/
```

Controllers handle HTTP concerns only. Services own business logic and transaction boundaries. Models contain no JPA annotations. Persistence is isolated in entities/repositories. Explicit mappers connect DTOs, models, and entities.

## Default stack

Unless explicitly overridden, newly generated projects use:

- Java 21
- Spring Boot 4.x
- Maven Wrapper
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- Microsoft SQL Server
- Microsoft JDBC Driver for SQL Server
- Flyway SQL Server support
- Lombok
- JUnit 6, Mockito, and AssertJ
- Spotless with Palantir Java Format

New persistent services use SQL Server by default. Do not silently substitute H2, PostgreSQL, MySQL, or another database.

When working inside an existing project, preserve its already configured datasource and database engine unless the user explicitly requests a migration or replacement.

For persistence and integration behavior that depends on database semantics, test against SQL Server rather than assuming H2 is equivalent.

## Generated local stack

New persistent microservices must include a complete Docker Compose stack containing the application, SQL Server, health checks, database initialization, and a named database volume.

The generated project must be startable with:

```bash
docker compose up -d --build
```

The verification process creates a record through the real HTTP API, confirms it directly in SQL Server, recreates the containers without deleting the volume, and verifies the same record again through both paths. See [`database.md`](docs/database.md) and [`testing.md`](docs/testing.md).

## Corporate SQL Server environments

Local Docker authentication is intentionally simpler than a corporate deployment.

The rules explicitly cover existing named datasources such as `spring.datasource.sqlserverdb`, including configurations that use:

- externally supplied host, port, database, username, and password;
- TLS/encryption options;
- NTLM/integrated security;
- trust stores;
- named persistence units;
- datasource-specific Hikari pools;
- platform secret groups such as `sql-server-billinguser`.

Agents must preserve those project-specific settings rather than replacing them with local defaults. Secret values must never be copied into source control.

See [`docs/database.md`](docs/database.md) for the complete rules.

## Flyway

Flyway owns schema evolution. Generated SQL Server projects use SQL Server-compatible migrations, and Hibernate validates the schema with `ddl-auto: validate`.

New migrations must use SQL Server/T-SQL-compatible types and syntax.

## Generated project quality gate

Generated Maven projects must provide the wrapper and pass:

```bash
./mvnw spotless:check && ./mvnw verify
```

## Recommended prompt

Because Java 21, Maven Wrapper, SQL Server, Flyway, Docker and the testing strategy are already defined by the repository, prompts do not need to repeat those defaults.

For example, to generate a complete microservice:

```text
Read `AGENTS.md` completely and read every relevant file under `docs/` before generating code.

Create a complete Spring Boot microservice following this repository's rules.

Project:
- name: orders-api
- group: com.acme
- artifact: orders-api
- base package: com.acme.orders

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

Generate everything required to build, test, run and verify the application.

Follow the repository defaults for Java, build tooling, database, persistence, migrations, Docker and testing.

Run formatting and the complete test suite.
Then start the complete Docker Compose stack, create a record through the real HTTP API, verify it directly in SQL Server, recreate the containers without deleting the volume, and verify that the same record still exists through both the API and SQL Server.

Do not stop until the generated project builds successfully and all required verification steps pass.
```

For a feature inside an existing project, the prompt can be shorter:

```text
Read `AGENTS.md` completely and every relevant file under `docs/`.

Add a Customer CRUD feature following the existing project conventions and this repository's rules.

Base path: /customers

Fields:
- id: Long, generated primary key
- firstName: String, required, max 100
- lastName: String, required, max 100
- email: String, required, valid email, unique, max 255
- active: Boolean, required, default true

Operations:
- create
- get by id
- paginated search by lastName/email/active
- patch
- delete

Add the required migration and tests.
Run formatting and the relevant test suite when finished.
```

For a more explicit starter prompt, see [`START_HERE.md`](START_HERE.md).

## Principle

The repository is intentionally opinionated. It stores only reusable rules and prompts; generated applications belong in their own directories and repositories.

## License

No license file is currently included. Add one before redistribution or external contributions if needed.
