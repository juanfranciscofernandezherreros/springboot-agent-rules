# Backend — Spring Boot

Standards: [`docs/java-style.md`](docs/java-style.md) · [`docs/annotations.md`](docs/annotations.md) ·
[`docs/layered-architecture.md`](docs/layered-architecture.md) · [`docs/controllers.md`](docs/controllers.md) ·
[`docs/mappers.md`](docs/mappers.md) · [`docs/exceptions.md`](docs/exceptions.md) ·
[`docs/testing.md`](docs/testing.md) · [`docs/logging.md`](docs/logging.md) ·
[`docs/database.md`](docs/database.md)

## Agent workflow

Before modifying or generating code:

1. Read this file completely.
2. Read every relevant file under `docs/`. Persistence work always requires `docs/database.md`.
3. Inspect existing project conventions before creating new files.
4. Do not introduce new dependencies unless required by the requested feature or selected database engine.
5. Follow the architecture documented here even if another Spring convention would also work.
6. Keep changes focused on the requested task.
7. Do not change public APIs, database schemas, datasource configuration, or architectural conventions unless explicitly requested.
8. After changes, run formatting and the relevant tests.
9. Prefer existing patterns over inventing new abstractions.
10. If generating a project from scratch, create all files necessary for it to build and run.
11. For a persistent project, ensure the selected database can be started locally and that the application configuration, JDBC driver, Flyway module, migrations, and documentation agree on the same engine.
12. A new persistent microservice must include a production-style `Dockerfile`, `.dockerignore`, and a Docker Compose stack that starts both the application and its database with health-aware dependencies and persistent database storage.
13. Do not describe a generated microservice as tested merely because unit tests pass. Build it, start the complete Compose stack, exercise the real HTTP API with `curl`, and verify the resulting row directly in the selected database.
14. Prove persistence by recreating the containers without deleting their named volumes, then read the same record through both the API and a direct database query.
15. Report concrete verification evidence: build/test result, container health, HTTP status and response, database row, restart result, and persistent volume name.

## Stack

Java 25 · Spring Boot 4.x · Maven or Gradle · Spring Data JPA · Lombok · JUnit 6 + Mockito + AssertJ.

The reference application currently demonstrates Microsoft SQL Server. The rules remain reusable for another database only when the user or the existing project explicitly selects it.

## Database and datasource rules

`docs/database.md` is the source of truth for datasource, SQL Server, Docker Compose, Flyway, multi-datasource, secret handling, and environment rules.

`docs/testing.md` is the source of truth for automated tests and the containerized runtime acceptance test required for newly generated persistent microservices.

Before creating or changing persistence code:

1. Inspect the existing datasource configuration, JDBC dependencies, Flyway modules, JPA configuration, persistence units, transaction managers, and migration scripts.
2. Reuse the project's existing database engine and datasource conventions unless the user explicitly requests another database.
3. Do not silently replace SQL Server, PostgreSQL, MySQL, or another configured engine.
4. Do not invent datasource names, schemas, connection properties, credentials, trust stores, authentication modes, or pool sizes.
5. Never hardcode production credentials or secret values.
6. If a new database engine or datasource is explicitly requested, add only the driver and database-specific support actually required.
7. Database migrations must use SQL compatible with the selected database engine.
8. Flyway owns deployed schema evolution; keep Hibernate schema handling at `validate` unless an established test profile intentionally uses otherwise.

### SQL Server reference behavior

When SQL Server is selected:

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

When generating a new persistent feature, determine from the request or existing project conventions:

- feature name;
- API base path;
- database engine and datasource when more than one exists;
- database schema when relevant;
- fields and Java types;
- required and optional fields;
- validation constraints;
- uniqueness constraints;
- relationships and foreign keys;
- generated/default values;
- supported CRUD operations;
- searchable/filterable fields;
- indexes and migration requirements.

Do not invent domain fields, relationships, uniqueness rules, database-specific behavior, or a new datasource when they are not present in the request or existing project.

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
- A persistent service is not complete until `docker compose up -d --build` starts the API and database successfully and a create/restart/read persistence check passes.
- Never use `docker compose down -v` during a persistence check. Volume deletion is destructive and requires an explicit request.
