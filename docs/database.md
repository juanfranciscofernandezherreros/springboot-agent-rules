# Database and Datasource Rules

Use these rules whenever persistence, datasource configuration, migrations, or database-specific tests are involved.

## General rules

- Inspect the existing JDBC driver, datasource properties, JPA configuration, Flyway modules, persistence units, transaction managers, schemas, and migrations before changing persistence code.
- Reuse the configured database engine unless the request explicitly asks for another engine.
- Never invent credentials, hosts, ports, schemas, datasource names, authentication modes, trust stores, pool sizes, or production connection options.
- Keep secrets externalized through environment variables or the platform secret/configuration mechanism.
- Flyway owns schema evolution. Hibernate should validate deployed schemas rather than create or mutate them.
- Migrations must be valid for the selected database engine.
- When database-specific behavior matters, test against the same engine rather than assuming H2 is equivalent.

## SQL Server reference standard

For Microsoft SQL Server:

- JDBC driver: `com.microsoft.sqlserver.jdbc.SQLServerDriver`.
- Maven driver artifact: `com.microsoft.sqlserver:mssql-jdbc`.
- Flyway SQL Server module: `org.flywaydb:flyway-sqlserver`.
- Hibernate dialect, only when explicitly configured by the project: `org.hibernate.dialect.SQLServerDialect`.
- JDBC URL form: `jdbc:sqlserver://<host>:<port>;databaseName=<database>`.
- Use SQL Server/T-SQL-compatible migrations: `IDENTITY`, `BIT`, SQL Server constraints and index syntax.
- Match temporal SQL types to Java semantics. Map `Instant` and `OffsetDateTime` to `DATETIMEOFFSET`; use `DATETIME2` for `LocalDateTime`. Do not use `DATETIME2` for an `Instant` merely because both contain date and time values.
- For database-generated UTC/offset timestamps stored as `DATETIMEOFFSET`, prefer an offset-aware default such as `SYSDATETIMEOFFSET()` and confirm that Hibernate schema validation accepts the resulting column type.
- Do not generate PostgreSQL constructs such as `SERIAL`, PostgreSQL-specific casts, `BOOLEAN` assumptions, expression indexes, or PostgreSQL-only functions.

## Local development with Docker Compose

When SQL Server is the selected production engine, prefer SQL Server locally as well.

A generated SQL Server project may use `mcr.microsoft.com/mssql/server:2022-latest` on port `1433` and create its explicitly requested local database through a one-shot initialization service.

Local defaults are development-only:

```text
host: localhost
port: 1433
database: <requested-local-database>
username: sa
password: LocalPassw0rd!
```

Rules:

- Local credentials may be simple development credentials but must never be presented as production defaults.
- Use `encrypt=true;trustServerCertificate=true` locally when appropriate for the containerized developer setup.
- Do not copy production NTLM, integrated-security, trust-store paths, or production pool sizing into local configuration unless the task explicitly requires reproducing them.
- `docker compose up -d` should leave the SQL Server service healthy and the reference database created before the application is started.
- Keep the local database data in a named Docker volume.

### Complete local microservice stack

For a newly generated persistent microservice, Compose must run the application as well as the database. Starting only the database is insufficient unless the user explicitly asks for a database-only development stack.

Include:

- a multi-stage `Dockerfile` that builds with the repository Maven/Gradle wrapper and runs the packaged application on a smaller JRE image;
- a `.dockerignore` that excludes build output, VCS metadata, IDE files, and other irrelevant local content;
- a non-root runtime user when the selected base image supports it;
- an application service built from the local `Dockerfile`;
- the selected database service backed by a named volume;
- database and application health checks;
- an idempotent one-shot database initialization service when the engine image does not create the requested database itself;
- dependency conditions that wait for database health and successful initialization rather than relying only on container start order;
- application datasource environment variables whose container hostname is the Compose database service name, not `localhost`;
- an exposed application port, preferably overridable by an environment variable when that matches project conventions;
- restart behavior appropriate for a local long-running application service.

The application container must run the same artifact and configuration model used outside Docker. Do not add a second source tree, bypass Flyway, or let Hibernate create the deployed schema just to make Compose start.

Validate the Compose model before startup:

```bash
docker compose config
docker compose up -d --build
docker compose ps -a
```

Treat the stack as ready only when the database and application report healthy and every required one-shot initializer exits with code `0`. If startup fails, inspect the relevant Compose logs and fix the root cause before running API checks.

### Persistence verification

For a new persistent service, verify the storage path end to end against the actual database engine:

1. Start the complete stack with `docker compose up -d --build`.
2. Wait for the application and database health checks to pass.
3. Create a uniquely identifiable record through the public HTTP API using `curl` and capture its identifier, HTTP status, and response body.
4. Query the application table directly using the database container's native client and confirm the created values.
5. Run `docker compose down` without `-v`, then start the stack again with `docker compose up -d`.
6. Retrieve the same identifier through the API and require a successful status and matching response.
7. Query the table directly again and confirm that the same row still exists.
8. Identify and report the named volume that retained the data.

Never run `docker compose down -v`, `docker volume rm`, or an equivalent volume-deleting command as part of this check. Deleting the volume invalidates the persistence proof and is destructive unless the user explicitly requests a clean reset.

Use unique test values so repeated verification runs do not collide with uniqueness constraints. Do not claim persistence from an API response alone: the direct database query before and after container recreation is required evidence.

## Deployed/corporate SQL Server configuration

When an existing deployment exposes a datasource such as:

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

preserve its shape and semantics unless the user explicitly requests a change.

The example above is not a template of default values. In particular:

- `sqlserverdb` is a project-specific datasource/persistence-unit name.
- NTLM and `integratedSecurity=true` are environment-specific authentication choices.
- trust-store location and password are environment-specific security configuration.
- Hikari values are capacity decisions and must not be copied blindly.
- `${HOSTNAME_SQL}`, `${PORT}`, `${DATABASENAME}`, `${USERNAME}`, `${PASSWORD}`, and `${TRUSTSTORE_PASS}` must be supplied externally.

If the deployment platform declares a secret group such as:

```yaml
- group: sql-server-billinguser
  scope: global
  secrets: true
```

treat that block as platform configuration, not Spring Boot syntax. Preserve the external-secret integration and never inline its secret values into application configuration or source control.

## Single datasource vs named/multiple datasources

A normal single datasource usually lives directly under `spring.datasource` and can use Spring Boot auto-configuration.

A named datasource such as `spring.datasource.sqlserverdb` usually means custom or multi-datasource configuration. Before changing it, locate:

- the matching `@ConfigurationProperties` binding;
- the `DataSource` bean;
- the `EntityManagerFactory`;
- the `PlatformTransactionManager`;
- `@EnableJpaRepositories` configuration;
- entity package ownership;
- persistence-unit name.

New repositories and entities must be attached to the datasource that owns that feature. Do not collapse multiple datasources into one default datasource. Do not assume cross-database transactions are atomic.

## Environment strategy

Prefer one codebase with environment-specific connection configuration:

- local: Docker Compose SQL Server with SQL authentication;
- deployed/corporate: externally supplied JDBC URL, credentials, TLS/authentication and trust-store settings;
- tests: unit/MVC tests should avoid a database when possible; persistence/integration tests should use SQL Server when database behavior is under test.

A generated simple single-datasource application may expose `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Existing enterprise applications may instead use named datasource properties and platform-specific secret injection; preserve the project's established convention.

## Flyway

- Every schema change gets a versioned migration.
- Never edit a migration that has already been applied in shared environments; create a new migration instead.
- Keep migrations deterministic and compatible with SQL Server.
- Do not use Hibernate `create`, `create-drop`, or `update` as a substitute for deployed schema migrations.
- Review destructive operations explicitly.

## New persistent feature checklist

Before generating a persistent feature, determine:

- feature name and API base path;
- database engine;
- datasource/persistence unit when more than one exists;
- schema;
- fields and Java types;
- nullability and validation rules;
- uniqueness constraints;
- relationships and foreign keys;
- generated/default values;
- CRUD operations;
- filters/search fields;
- required indexes;
- migration requirements.

Do not invent missing domain requirements. Follow existing project conventions when they resolve an unspecified implementation detail.
