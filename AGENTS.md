# Backend — Spring Boot

Standards: [`docs/domain-contract.md`](docs/domain-contract.md) · [`docs/java-style.md`](docs/java-style.md) ·
[`docs/annotations.md`](docs/annotations.md) · [`docs/layered-architecture.md`](docs/layered-architecture.md) ·
[`docs/controllers.md`](docs/controllers.md) · [`docs/mappers.md`](docs/mappers.md) ·
[`docs/exceptions.md`](docs/exceptions.md) · [`docs/testing.md`](docs/testing.md) ·
[`docs/logging.md`](docs/logging.md) · [`docs/database.md`](docs/database.md)

## Agent workflow

Before modifying or generating code:

1. Read this file completely.
2. Read every relevant file under `docs/`. Persistence work always requires `docs/database.md`. New feature/API generation always requires `docs/domain-contract.md`.
3. Inspect existing project conventions before creating new files.
4. Establish the domain contract from the user's request and existing project sources before generating production code. Do not silently invent business fields, states, transitions, validation rules, endpoints, identifiers, financial rules, security behavior, or persistence semantics.
5. Do not introduce new dependencies unless required by the requested feature or selected database engine.
6. Follow the architecture documented here even if another Spring convention would also work.
7. Keep changes focused on the requested task.
8. Do not change public APIs, database schemas, datasource configuration, or architectural conventions unless explicitly requested.
9. Prefer existing patterns over inventing new abstractions.
10. If generating a project from scratch, create all files necessary for it to build and run.
11. New generated projects use Java 21 unless the user explicitly requests another supported Java version.
12. New persistent projects use Microsoft SQL Server by default. Do not silently substitute H2, PostgreSQL, MySQL, or another engine.
13. For an existing project, preserve an already configured datasource and database engine unless the user explicitly requests a migration or replacement.
14. For a persistent project, ensure SQL Server or the explicitly selected existing engine can be started locally and that the application configuration, JDBC driver, Flyway module, migrations, and documentation agree on the same engine.
15. A new persistent microservice must include a production-style `Dockerfile`, `.dockerignore`, and a Docker Compose stack that starts both the application and SQL Server with health-aware dependencies and persistent database storage.
16. After source generation or modification, run `spotless:apply` before `spotless:check`. Formatting changes are part of the implementation and must be included before compilation or publication.
17. Before commit, push, PR creation, or reporting completion, run the mandatory finalization sequence from `docs/testing.md`: formatting apply/check, production compile/package, full verification, and CI parity gate. Every required command must exit with code `0`.
18. When GitHub Actions exists, run the exact Maven Wrapper command used by the workflow before publishing changes. CI must verify committed code; do not use CI `spotless:apply` to hide formatting failures.
19. Do not describe a generated microservice as tested merely because unit tests pass. Build it, start the complete Compose stack, exercise the real HTTP API with `curl`, and verify the resulting row directly in SQL Server.
20. Prove persistence by recreating the containers without deleting their named volumes, then read the same record through both the API and a direct SQL Server query.
21. Report concrete verification evidence: formatting result, production compile/package result, test result, CI parity result, container health, HTTP status and response, database row, restart result, and persistent volume name.
22. If a required check cannot run because the environment lacks Java, Maven prerequisites, Docker, network access, or another dependency, say exactly what was not executed. Never claim compiled, tested, persistence-verified, CI-ready, or complete without corresponding execution evidence.
23. When adding GitHub Actions, keep compile/package and behavioral/test concerns independently diagnosable.
24. A compile-only Maven job must use `-Dmaven.test.skip=true` when test sources must not be compiled. Do not assume `-DskipTests` skips test compilation.
25. In Spring Boot 4 projects, verify test starter modularization before using MVC test slices; `@WebMvcTest` may require `spring-boot-starter-webmvc-test` in addition to `spring-boot-starter-test`.
26. When Spring Data exposes overloaded repository methods, use typed Mockito matchers such as `any(<Feature>Entity.class)` to avoid compile-time ambiguity.
27. When Cucumber is requested, configure its JUnit Platform engine explicitly and prove that Maven discovers and executes at least one scenario before considering the workflow complete.

## Mandatory finalization sequence

The following sequence is part of implementation, not optional cleanup:

```bash
./mvnw --batch-mode --no-transfer-progress spotless:apply
./mvnw --batch-mode --no-transfer-progress spotless:check
./mvnw --batch-mode --no-transfer-progress -Dmaven.test.skip=true clean package
./mvnw --batch-mode --no-transfer-progress verify
./mvnw --batch-mode --no-transfer-progress spotless:check verify
```

Rules:

- Do not commit, push, open a pull request, or report completion while any command fails.
- If `spotless:apply` modifies files, rerun subsequent checks against the formatted files.
- If source, test, dependency, build, formatter, or workflow configuration changes after a passing gate, rerun the affected checks; when uncertain, rerun the entire sequence.
- The last command is the default CI parity gate. If the repository workflow uses another Maven Wrapper command, that exact command is an additional mandatory pre-publication gate.
- A generated project that has not passed production compilation is not complete, even when tests were not requested explicitly.

## Stack

Java 21 · Spring Boot 4.x · Maven Wrapper · Spring Data JPA · Lombok · JUnit 6 + Mockito + AssertJ.

Microsoft SQL Server is the default persistence engine for new generated services. Use another engine only when the user explicitly requests it or an existing project already defines a different datasource that must be preserved.

## Domain contract rules

`docs/domain-contract.md` is the source of truth for determining feature inputs and preventing silent invention of business requirements.

Before generating a feature, determine from the request or existing project sources, as applicable:

- feature name and API base path;
- operations and HTTP methods;
- request/response fields and Java/wire types;
- required and optional fields;
- validation constraints;
- identifiers and generation strategy;
- enums, allowed values and state transitions;
- business invariants;
- uniqueness constraints;
- relationships and foreign keys;
- generated/default values;
- supported CRUD/search behavior;
- pagination, filters and sorting;
- idempotency requirements;
- error cases and HTTP status mapping;
- authentication/authorization and audit requirements when present;
- indexes and migration requirements.

Do not present implementation assumptions as established business requirements. For financial, regulated, accounting, or security behavior, never invent fees, balance semantics, settlement guarantees, fraud rules, transfer limits, authorization policy, idempotency guarantees, or state transitions.

## Database and datasource rules

`docs/database.md` is the source of truth for datasource, SQL Server, Docker Compose, Flyway, multi-datasource, secret handling, and environment rules.

`docs/testing.md` is the source of truth for automated tests, CI behavior, finalization gates, and the containerized runtime acceptance test required for newly generated persistent microservices.

Before creating or changing persistence code:

1. Inspect the existing datasource configuration, JDBC dependencies, Flyway modules, JPA configuration, persistence units, transaction managers, and migration scripts.
2. For a new generated project with no existing datasource, use Microsoft SQL Server.
3. For an existing project, reuse its configured database engine and datasource conventions unless the user explicitly requests another database.
4. Do not silently replace an existing configured engine.
5. Do not invent datasource names, schemas, connection properties, credentials, trust stores, authentication modes, or pool sizes.
6. Never hardcode production credentials or secret values.
7. If a different database engine or datasource is explicitly requested, add only the driver and database-specific support actually required.
8. Database migrations must use SQL compatible with the selected database engine.
9. Flyway owns deployed schema evolution; keep Hibernate schema handling at `validate` unless an established test profile intentionally uses otherwise.

### SQL Server reference behavior

When SQL Server is selected — which is the default for new generated persistent services:

- Use `com.microsoft.sqlserver.jdbc.SQLServerDriver` and the `com.microsoft.sqlserver:mssql-jdbc` artifact.
- Use Flyway SQL Server support (`org.flywaydb:flyway-sqlserver`).
- Use SQL Server-compatible Flyway migrations.
- Use `org.hibernate.dialect.SQLServerDialect` only when the project explicitly configures the dialect.
- For local development, prefer the repository Docker Compose SQL Server and simple SQL authentication.
- For deployed/corporate environments, preserve existing TLS, NTLM/integrated-security, trust-store, datasource name, persistence-unit, schema, secret injection, and Hikari settings rather than replacing them with local defaults.
- Local development configuration and corporate configuration are intentionally different connection profiles for the same database engine.

A corporate datasource may look like:

```yaml
spring:
  datasource:
    sqlserverdb:
      url: jdbc:sqlserver://${HOSTNAME_SQL}:${PORT};database=${DATABASENAME};encrypt=true;trustServerCertificate=true;authenticationScheme=NTLM;integratedSecurity=true;trustStore=/deployments/crypto-stores/truststore.jks;trustStorePassword=${TRUSTSTORE_PASS}
      username: ${USERNAME}
      password: ${PASSWORD}
      driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
      dialect: org.hibernate.dialect.SQLServerDialect
      ddl-auto: validate
      persistence-unit: sqlserverdb
      hikari:
        connection-timeout: 50000
        idle-timeout: 300000
        max-lifetime: 900000
        maximum-pool-size: 200
        minimum-idle: 80
        pool-name: ConnPoolBilling
```

These values are an example of an existing project-specific shape, not defaults. Never copy its pool sizes, authentication settings, trust-store path, persistence-unit name, or secret values into an unrelated project.

A deployment block such as:

```yaml
- group: sql-server-billinguser
  scope: global
  secrets: true
```

is platform/secret configuration, not Spring Boot configuration. Preserve the external secret integration and never inline the secrets in source control.

### Multiple datasources

When a project contains named datasources such as `spring.datasource.sqlserverdb`, treat it as custom or multi-datasource configuration until proven otherwise.

Before modifying it:

- locate the corresponding `@ConfigurationProperties`, `DataSource`, `EntityManagerFactory`, `PlatformTransactionManager`, repository configuration, entity packages, and persistence-unit setup;
- determine which datasource owns the feature's entities and repositories;
- bind new repositories and entities to the correct persistence unit and transaction manager;
- do not move a feature between datasources unless explicitly requested;
- do not collapse multiple datasources into the default Spring Boot datasource;
- do not assume cross-database operations are atomic or introduce distributed transactions unless explicitly required.

### New persistent feature inputs

When generating a new persistent feature, use `docs/domain-contract.md` first, then determine persistence-specific inputs from the request or existing project conventions:

- datasource when more than one exists;
- database schema when relevant;
- relationships and foreign keys;
- uniqueness constraints;
- indexes and migration requirements.

For new projects, the database engine is SQL Server unless explicitly overridden. Do not invent domain fields, relationships, uniqueness rules, database-specific behavior, or a new datasource when they are not present in the request or existing project.

## Feature package layout

One package per feature, split into layer subpackages:

```text
com/<company>/<app>/<feature>/
  controller/   <Feature>Controller     @RestController @RequestMapping("/<feature>")
  service/      <Feature>Service        interface — the feature's public surface
                <Feature>ServiceImpl    @Service, class-level @Transactional — the one impl
  repository/   <Feature>Repository     interface extends JpaRepository<<Feature>Entity, Id>
  model/        <Feature>, enums        plain POJO (Lombok), no jakarta.persistence imports
  entity/       <Feature>Entity         @Entity only, no logic
  dto/          Create/Update/Response records
  mapper/       <Feature>Mapper         static — DTO ↔ model
                <Feature>EntityMapper   static — model ↔ entity
```

Not every feature needs every file. Add a layer only when it actually carries weight.

## Gotchas

- The service is an interface `<Feature>Service` plus one `@Service` implementation `<Feature>ServiceImpl`.
- Inject and mock the interface. In the service unit test, `@InjectMocks` targets `<Feature>ServiceImpl`.
- The repository is a plain Spring Data interface. "Find or 404" is a service concern.
- `model/` holds plain POJOs with no JPA imports; `entity/` holds persistence classes only.
- No `final` on method parameters or local variables. Keep `final` only on constructor-injected fields when Lombok requires it.
- Use `var` in controllers and tests; explicit types in services, mappers, and the rest of production code.
- No existence checks or business logic in controllers.
- `@Transactional` belongs at class level on service implementations; use `readOnly = true` for read-only services/paths where applicable.
- Always run `spotless:apply` before the final `spotless:check`; CI does not repair formatting.
- Spring Boot 4 test dependencies are modular. Do not assume every test annotation comes from `spring-boot-starter-test`.
- Maven `-DskipTests` still compiles tests; use `-Dmaven.test.skip=true` for a truly compile/package-only CI job.
- Cucumber selection with `-Dtest=CucumberTest` does not bypass compilation of unrelated JUnit tests; the complete test source set must compile.
- Mockito `any()` can become ambiguous when Spring Data adds overloads; use typed matchers for overloaded repository methods.
- A persistent service is not complete until `docker compose up -d --build` starts the API and SQL Server successfully and a create/restart/read persistence check passes.
- Never use `docker compose down -v` during a persistence check. Volume deletion is destructive and requires an explicit request.
