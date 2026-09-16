# Spring Boot Agent Rules

A practical, opinionated ruleset for AI coding agents that generate and maintain Spring Boot backends with consistent architecture, naming, testing, database handling, and code style.

The repository combines:

1. **Agent instructions and engineering standards** in [`AGENTS.md`](AGENTS.md) and [`docs/`](docs/).
2. **A runnable reference Task API** showing those conventions in a real Spring Boot project.

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

## Reference stack

The included reference application uses:

- Java 25
- Spring Boot 4.0.0
- Maven Wrapper
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- Microsoft SQL Server
- Microsoft JDBC Driver for SQL Server
- Flyway SQL Server support
- Lombok
- JUnit Platform
- Cucumber
- H2 only for the lightweight Cucumber test profile, configured in SQL Server compatibility mode
- Spotless with Palantir Java Format

For persistence/integration behavior that depends on SQL Server semantics, the rules require testing against SQL Server rather than assuming H2 is equivalent.

## Local SQL Server

The reference application runs against a real SQL Server container locally.

Start it with:

```bash
docker compose up -d
```

The Compose setup starts SQL Server on port `1433`, waits for it to become healthy, and creates the `tasks` database through a one-shot initialization service.

Local development defaults:

```text
Database: tasks
Username: sa
Password: LocalPassw0rd!
Port: 1433
```

These credentials are development-only and are not production defaults.

Run the application:

```bash
./mvnw spring-boot:run
```

The default local JDBC URL is:

```text
jdbc:sqlserver://localhost:1433;databaseName=tasks;encrypt=true;trustServerCertificate=true
```

Override connection values using:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

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

Flyway owns schema evolution. The reference application uses SQL Server-compatible migrations and Hibernate validates the schema with `ddl-auto: validate`.

New migrations must use SQL Server/T-SQL-compatible types and syntax when SQL Server is the selected engine.

## Task API

The reference API exposes:

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/tasks` | Create a task |
| `GET` | `/tasks/{id}` | Get a task by ID |
| `GET` | `/tasks/search` | Search and paginate tasks |
| `PATCH` | `/tasks/{id}` | Partially update a task |
| `DELETE` | `/tasks/{id}` | Delete a task |

## Development commands

Run tests:

```bash
./mvnw test
```

Apply formatting:

```bash
./mvnw spotless:apply
```

Run the full verification lifecycle:

```bash
./mvnw verify
```

## Generating a new feature

A persistent feature request should specify, or allow the existing project to determine:

- feature and API path;
- database engine/datasource;
- schema when relevant;
- fields and Java types;
- validation/nullability;
- unique constraints;
- relationships;
- CRUD operations;
- searchable/filterable fields;
- indexes/migration requirements.

Example:

```text
Read AGENTS.md completely and every relevant document under docs/.
Persistence work must follow docs/database.md.

Create a Customer CRUD feature.

Database: existing SQL Server datasource
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

Add the SQL Server-compatible Flyway migration.
Add service unit tests and MVC controller tests.
Do not introduce unrelated dependencies.
Run formatting and tests when finished.
```

For a complete generation prompt, see [`START_HERE.md`](START_HERE.md).

## Principle

The repository is intentionally opinionated. The important part is not that every project use exactly these conventions; it is that architecture, database behavior, environment configuration, and testing decisions are explicit enough that humans and coding agents can apply them consistently.

## License

No license file is currently included. Add one before redistribution or external contributions if needed.
