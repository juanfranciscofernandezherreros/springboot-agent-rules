# Spring Boot Agent Rules

A practical, opinionated ruleset for AI coding agents that generate and maintain Spring Boot backends with consistent architecture, naming, testing, database handling, and code style.

The repository contains agent instructions and engineering standards in [`AGENTS.md`](AGENTS.md) and [`docs/`](docs/). It intentionally contains no generated microservice, build output, or database runtime.

## Normative sources and precedence

`README.md` and `START_HERE.md` are explanatory documents only. They must not redefine mandatory commands, publication gates, stack defaults, or behavioral rules.

When instructions overlap, use this precedence:

1. [`AGENTS.md`](AGENTS.md) defines mandatory agent behavior and rule precedence.
2. The relevant document under [`docs/`](docs/) defines the canonical subject-specific rule.
3. `README.md` and `START_HERE.md` explain and link to those rules but do not override them.

If explanatory documentation conflicts with a higher-precedence source, follow the higher-precedence source and fix the explanatory document.

## Canonical standards

| Document | Canonical responsibility |
| --- | --- |
| [`domain-contract.md`](docs/domain-contract.md) | Feature/API contract and assumption control |
| [`java-style.md`](docs/java-style.md) | Java style and formatter behavior |
| [`annotations.md`](docs/annotations.md) | Annotation usage and placement |
| [`layered-architecture.md`](docs/layered-architecture.md) | Feature-oriented layered architecture |
| [`controllers.md`](docs/controllers.md) | REST controller conventions |
| [`mappers.md`](docs/mappers.md) | DTO, model, and entity mappings |
| [`exceptions.md`](docs/exceptions.md) | Application/API error handling |
| [`testing.md`](docs/testing.md) | Tests, CI, finalization gates, verification states, publication rules |
| [`logging.md`](docs/logging.md) | Logging conventions |
| [`database.md`](docs/database.md) | SQL Server, Docker, Flyway, datasources, secrets, multi-datasource rules |

Persistence work must read `docs/database.md`. New features and public API work must read `docs/domain-contract.md`.

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

## Default stack overview

Unless explicitly overridden or an existing project establishes another supported convention, newly generated services use:

- Java 21;
- Spring Boot 4.x;
- Maven Wrapper;
- Spring MVC using `spring-boot-starter-webmvc` for newly generated Boot 4 MVC services;
- Spring Data JPA;
- Bean Validation;
- Microsoft SQL Server for new persistent services;
- Microsoft JDBC Driver for SQL Server;
- Flyway SQL Server support;
- Lombok;
- JUnit 6, Mockito, and AssertJ;
- Spotless with Palantir Java Format.

Existing projects keep their configured datasource, database engine, web stack, and established conventions unless the user explicitly requests a migration or replacement.

## Finalization and publication

There is exactly one canonical definition of the Maven finalization sequence and publication rules: [`docs/testing.md`](docs/testing.md).

Do not copy, shorten, reorder, or redefine that command sequence in this README. Generated or modified code is not considered verified merely because files were written or because CI may run later.

CI verifies committed code; it does not replace mandatory pre-publication formatting and verification when the agent can execute those checks.

## Generated local stack

New persistent microservices must include a complete Docker Compose stack containing the application, SQL Server, health checks, database initialization when required, and a named database volume. Runtime acceptance and persistence verification are defined in [`docs/testing.md`](docs/testing.md) and [`docs/database.md`](docs/database.md).

## Flyway

Flyway owns schema evolution. Generated SQL Server projects use SQL Server-compatible migrations, and Hibernate validates the schema rather than mutating it in deployed environments.

## Recommended usage

Start with [`START_HERE.md`](START_HERE.md), then read `AGENTS.md` and every relevant canonical document under `docs/` before generating or modifying code.

## Principle

The repository is intentionally opinionated. It stores reusable rules and prompts; generated applications belong in their own directories and repositories.

## License

No license file is currently included. Add one before redistribution or external contributions if needed.
