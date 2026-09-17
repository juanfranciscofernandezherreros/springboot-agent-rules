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

## Complete API-first prompt: `investment-funds-api`

Use the following prompt with an agent working from this repository. It is deliberately explicit about the product contract while delegating workflow, verification, and publication rules to the canonical documents.

```text
Read AGENTS.md completely and every relevant canonical document under docs/ before generating code. Apply docs/domain-contract.md, database.md, layered-architecture.md, controllers.md, mappers.md, exceptions.md, testing.md, java-style.md, annotations.md and logging.md.

Generate a complete Spring Boot microservice from scratch.

Project:
- name and artifact: investment-funds-api
- group and base package: com.example / com.example.investmentfunds
- Java 21, Spring Boot 4.x, Maven Wrapper
- Spring MVC, Spring Data JPA, Bean Validation, Lombok
- Microsoft SQL Server and Flyway

Use API-first. src/main/resources/static/openapi.yaml is the public contract source of truth. The Maven build must validate it and generate the Spring server interface with OpenAPI Generator; the controller must implement the generated interface. Do not keep a handwritten duplicate API interface.

Feature: investment funds. Base path: /api/v1/funds.

Fund fields:
- id: Long, SQL Server BIGINT IDENTITY primary key, generated
- isin: String required, max 12, unique
- name: String required, max 200
- managementCompany: String optional, max 200
- category: String optional, max 100
- currency: String required, exactly 3 characters
- inceptionDate and navDate: LocalDate optional
- nav, assetsUnderManagement, managementFee, depositFee, ter, returnYtd, return1Year, return3Years, return5Years: BigDecimal optional
- investors: Integer optional
- riskLevel: Integer optional, between 1 and 7 when present
- active: Boolean required, default true
- createdAt and updatedAt: OffsetDateTime; created on creation and updated only on modification

Do not invent financial calculations, recommendations, authentication, authorization, business states, relationships, fields or filters.

Operations:
- POST /api/v1/funds returns 201
- GET /api/v1/funds/{id} returns 200 or standard 404
- GET /api/v1/funds supports Spring Data pagination and only isin, name, managementCompany, category, currency, riskLevel and active filters
- PATCH /api/v1/funds/{id} changes only fields present; never id or createdAt
- DELETE /api/v1/funds/{id} is a physical deletion

Architecture:
- feature package com.example.investmentfunds.fund with controller, service, repository, model, entity, dto and mapper
- FundController implements the OpenAPI-generated server interface; it never accesses repository
- FundService plus FundServiceImpl own transactions, find-or-404 and uniqueness
- FundRepository is Spring Data; model has no JPA imports; explicit DTO/model/entity mappers have no I/O

Database:
- versioned Flyway SQL Server migration creates investment_funds
- BIGINT IDENTITY id, unique isin, nullable risk_level CHECK 1..7, DATETIMEOFFSET timestamps
- Hibernate validates; never use H2
- externalize DB_URL, DB_USERNAME and DB_PASSWORD

Docker:
- production-style multi-stage Dockerfile, .dockerignore and compose.yaml
- application, SQL Server, idempotent database initialization, health checks and named persistent volume
- compose dependencies wait for database health and initialization
- add a non-destructive PowerShell runtime acceptance script: create through API, query SQL Server directly, docker compose down without -v, restart, retrieve through API, query SQL Server again and identify retained volume

Tests and quality:
- service unit tests, MVC tests and Cucumber JUnit Platform scenarios
- Cucumber covers valid and invalid POST, GET, search, PATCH and DELETE, prints each request and response, and emits target/cucumber/cucumber.html and cucumber.json
- configure JaCoCo to enforce at least 80 percent line coverage of application logic
- use Cucumber's JUnit Platform engine explicitly and prove Maven discovers scenarios
- CI uses Maven Wrapper and runs spotless:check verify on push and pull request

README must distinguish supplied requirements, inherited rules and implementation decisions, and document build, run, Docker, endpoints, curl examples, tests and reports.

Before reporting completion, execute docs/testing.md's canonical Maven finalization sequence exactly. If Docker is available, execute docs/database.md's runtime persistence acceptance. Never report a stronger verification state than actually passed.
```

## License

No license file is currently included. Add one before redistribution or external contributions if needed.
