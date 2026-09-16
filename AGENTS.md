# Backend — Spring Boot

Standards: [`docs/java-style.md`](docs/java-style.md) · [`docs/annotations.md`](docs/annotations.md) ·
[`docs/layered-architecture.md`](docs/layered-architecture.md) · [`docs/controllers.md`](docs/controllers.md) ·
[`docs/mappers.md`](docs/mappers.md) · [`docs/exceptions.md`](docs/exceptions.md) ·
[`docs/testing.md`](docs/testing.md) · [`docs/logging.md`](docs/logging.md)

## Agent workflow

Before modifying or generating code:

1. Read this file completely.
2. Read the relevant files under `docs/`.
3. Inspect existing project conventions before creating new files.
4. Do not introduce new dependencies unless required by the requested feature.
5. Follow the architecture documented here even if another Spring convention would also work.
6. Keep changes focused on the requested task.
7. Do not change public APIs, database schemas, datasource configuration, or architectural conventions unless explicitly requested.
8. After changes, run formatting and the relevant tests.
9. Prefer existing patterns over inventing new abstractions.
10. If generating a project from scratch, create all files necessary for it to build and run.

## Stack

Java 25 · Spring Boot 4.x · Maven or Gradle · Spring Data JPA · Lombok · JUnit 6 + Mockito + AssertJ.

## Database and datasource rules

The ruleset is database-engine agnostic unless the existing project or the feature request selects a specific engine.

Before creating or changing persistence code:

1. Inspect the existing datasource configuration, JDBC dependencies, Flyway modules, JPA configuration, persistence units, transaction managers, and migration scripts.
2. Reuse the project's existing database engine and datasource conventions unless the user explicitly requests another database.
3. Do not silently replace PostgreSQL, SQL Server, MySQL, or another configured engine.
4. Do not invent datasource names, schemas, connection properties, credentials, trust stores, authentication modes, or pool sizes.
5. Never hardcode credentials or secret values. Keep usernames, passwords, trust-store passwords, hosts, ports, and database names externally configured.
6. If the requested feature requires a new database engine or datasource, add only the driver and database-specific support that are actually required.
7. Database migrations must use SQL compatible with the selected database engine.
8. Prefer schema validation in deployed environments. Do not rely on Hibernate to create or mutate production schemas when Flyway owns schema evolution.

### SQL Server

When the existing project uses Microsoft SQL Server:

- Use the Microsoft JDBC driver: `com.microsoft.sqlserver.jdbc.SQLServerDriver`.
- Use `org.hibernate.dialect.SQLServerDialect` when the project explicitly configures a Hibernate dialect.
- Preserve existing SQL Server JDBC URL options such as encryption, certificate handling, integrated authentication, NTLM settings, and trust-store configuration. Do not add or remove those options unless explicitly requested.
- Keep connection values externalized, for example host, port, database name, username, password, and trust-store password.
- When Flyway is used, generate SQL Server-compatible migrations rather than PostgreSQL- or H2-specific SQL.
- Respect the existing schema, including `dbo` or any project-specific schema. Do not assume `dbo` when another schema is configured.
- If `ddl-auto` is already configured as `validate`, preserve it unless the task explicitly requires a configuration change.

Example of an existing named SQL Server datasource configuration that must be preserved when encountered:

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

The values above are an example of a project-specific datasource shape, not defaults for new projects. In particular, pool sizing, authentication mode, TLS options, trust-store paths, and persistence-unit names must come from the existing project or from explicit requirements.

### Multiple datasources

When a project contains named datasources such as `spring.datasource.sqlserverdb` instead of the standard single `spring.datasource` configuration, treat it as a custom or multi-datasource setup until proven otherwise.

Before modifying such a project:

- Locate and inspect the corresponding `@ConfigurationProperties`, `DataSource`, `EntityManagerFactory`, `PlatformTransactionManager`, repository configuration, and persistence-unit setup.
- Determine which datasource owns the feature's entities and repositories.
- Bind new repositories and entities to the correct persistence unit and transaction manager.
- Do not move a feature from one datasource to another unless explicitly requested.
- Do not collapse multiple datasources into the default Spring Boot datasource configuration.
- Use the transaction manager associated with the datasource that owns the operation.
- Cross-database operations must not be assumed to be atomic. Do not introduce distributed transactions unless explicitly required.

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
- relationships;
- generated/default values;
- supported CRUD operations;
- searchable and filterable fields.

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
