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
- Use SQL Server/T-SQL-compatible migrations: `IDENTITY`, `BIT`, `DATETIME2`, SQL Server constraints and index syntax.
- Do not generate PostgreSQL constructs such as `SERIAL`, PostgreSQL-specific casts, `BOOLEAN` assumptions, expression indexes, or PostgreSQL-only functions.

## Local development with Docker Compose

When SQL Server is the selected production engine, prefer SQL Server locally as well.

The reference application uses `mcr.microsoft.com/mssql/server:2022-latest` on port `1433` and creates the local `tasks` database through a one-shot initialization service.

Local defaults are development-only:

```text
host: localhost
port: 1433
database: tasks
username: sa
password: LocalPassw0rd!
```

Rules:

- Local credentials may be simple development credentials but must never be presented as production defaults.
- Use `encrypt=true;trustServerCertificate=true` locally when appropriate for the containerized developer setup.
- Do not copy production NTLM, integrated-security, trust-store paths, or production pool sizing into local configuration unless the task explicitly requires reproducing them.
- `docker compose up -d` should leave the SQL Server service healthy and the reference database created before the application is started.
- Keep the local database data in a named Docker volume.

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

The application may expose `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for a simple single-datasource reference app. Existing enterprise applications may instead use named datasource properties and platform-specific secret injection; preserve the project's established convention.

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
