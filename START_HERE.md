# Start here

This repository is a ruleset for AI coding agents. It intentionally contains no runnable Spring Boot application; generate each microservice in its own directory or repository.

`START_HERE.md` is explanatory only. It does not redefine mandatory rules. Read [`AGENTS.md`](AGENTS.md) first, then every relevant canonical document under [`docs/`](docs/).

## Rule precedence

When instructions overlap:

1. `AGENTS.md` defines mandatory agent behavior.
2. The relevant document under `docs/` defines the canonical subject-specific rule.
3. `README.md` and `START_HERE.md` explain usage but do not override mandatory rules.

Any persistence task must include `docs/database.md`. Any new feature or public API generation must include `docs/domain-contract.md`.

## Default generation baseline

Unless the user explicitly requests otherwise, or an existing project establishes another supported convention, new Spring Boot services use:

- Java 21;
- Maven Wrapper;
- Spring Boot 4.x;
- Spring MVC with `spring-boot-starter-webmvc` for newly generated Boot 4 MVC services;
- Microsoft SQL Server for new persistent services;
- SQL Server-compatible Flyway migrations;
- Docker Compose with the application and SQL Server;
- externalized deployed credentials and connection settings;
- Spotless with Palantir Java Format;
- automated tests appropriate to the feature.

Do not substitute H2, PostgreSQL, MySQL, or another database for SQL Server in a newly generated persistent service unless the user explicitly asks for a different engine.

## Before generating domain code

Establish the feature contract from the user's request, existing API specifications, code, tests, migrations, and documentation. Do not silently invent business fields, states, transitions, validation rules, endpoints, identifiers, uniqueness constraints, financial rules, security behavior, or persistence semantics.

When assumptions are unavoidable, keep them minimal and reversible and document them explicitly as assumptions. Follow `docs/domain-contract.md`.

## Completion and publication

Generation is not complete when files have merely been written.

The only canonical Maven finalization sequence, verification-state model, and publication policy are defined in [`docs/testing.md`](docs/testing.md). Do not copy or shorten that sequence here.

Important consequences:

- formatting must be applied before it is checked;
- a revision must not be described with a stronger verification state than the gates that actually passed;
- CI verifies committed code but does not replace executable pre-publication gates;
- an `UNVERIFIED` Java revision must not be pushed to the default branch;
- if the user explicitly insists on publishing an unverified revision, use a clearly named non-default branch such as `unverified/<description>` or `wip/<description>` and state the missing gates.

For a new persistent microservice, continue with the runtime acceptance test required by `docs/testing.md`: validate Compose, build and start the full stack, verify container health, create through the real API, query SQL Server directly, restart containers without deleting volumes, and prove the same row still exists through both API and SQL Server.

## Example prompt

```text
Read AGENTS.md completely and every relevant canonical document under docs/ before generating code.
Persistence work must follow docs/database.md.
New feature/API work must follow docs/domain-contract.md.
Verification and publication must follow docs/testing.md exactly.

Create a complete Spring Boot application from scratch with the repository defaults.

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
Follow AGENTS.md strictly and execute the canonical finalization and runtime-verification rules from docs/testing.md.
```

## Existing corporate datasource

If the target application already exposes a named datasource such as `spring.datasource.sqlserverdb`, do not replace it with the simple reference-app datasource. Inspect its datasource configuration, persistence unit, entity manager, transaction manager, repository ownership, secret injection, NTLM/TLS/trust-store settings and Hikari configuration, then extend the existing pattern.

If an existing target project already uses a different database engine, preserve it unless the user explicitly requests migration to SQL Server.

The local Docker configuration is a developer convenience. It must not overwrite corporate authentication or secret-management conventions.
