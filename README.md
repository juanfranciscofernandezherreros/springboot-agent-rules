# Spring Boot Agent Rules

A practical, opinionated ruleset and Python CLI for AI coding agents that generate and maintain Spring Boot microservices with consistent architecture, naming, testing, SQL Server handling and code style.

The repository separates three concerns:

- `AGENTS.md` and `docs/` define **how** Spring Boot code must be built.
- YAML service specifications define **what** microservice or feature must be built.
- the Python CLI turns the specification into an agent prompt, invokes the configured coding agent, validates the result and can run the full verification flow.

Generated microservices belong under `generated/` by default and are ignored by Git in this rules repository.

## Default stack

Unless explicitly overridden for an existing project, new generated persistent services use:

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
- JUnit 6, Mockito and AssertJ
- Spotless with Palantir Java Format

SQL Server is the default persistence engine. Do not silently substitute H2, PostgreSQL, MySQL or another engine. Existing projects keep their already configured datasource unless a migration is explicitly requested.

## Rules

`AGENTS.md` is the root instruction file. Detailed standards live under `docs/`:

| Document | Purpose |
| --- | --- |
| [`java-style.md`](docs/java-style.md) | Java style and formatting |
| [`annotations.md`](docs/annotations.md) | Annotation usage and placement |
| [`layered-architecture.md`](docs/layered-architecture.md) | Feature-oriented layered architecture |
| [`controllers.md`](docs/controllers.md) | REST controller conventions |
| [`mappers.md`](docs/mappers.md) | DTO, model and entity mappings |
| [`exceptions.md`](docs/exceptions.md) | Application/API error handling |
| [`testing.md`](docs/testing.md) | Unit, MVC, integration and runtime testing conventions |
| [`logging.md`](docs/logging.md) | Logging conventions |
| [`database.md`](docs/database.md) | SQL Server, Docker, Flyway, datasources, secrets and multi-datasource rules |
| [`cli.md`](docs/cli.md) | Python CLI generation and verification workflow |

Persistence work must read `docs/database.md` in addition to the architectural rules.

## Install the CLI

Python 3.11 or newer is required.

```bash
python -m venv .venv
source .venv/bin/activate
python -m pip install -e '.[dev]'
```

You can then use either:

```bash
agent-rules --help
```

or:

```bash
python -m agent_rules --help
```

## Describe the microservice in YAML

The YAML contains business inputs, not framework boilerplate. Framework and database defaults come from the rules repository.

```yaml
project:
  name: orders-api
  group: com.acme
  package: com.acme.orders

feature:
  name: Order
  basePath: /orders

  fields:
    - name: id
      type: Long
      primaryKey: true
      generated: true

    - name: customerReference
      type: String
      required: true
      max: 100

    - name: status
      type: OrderStatus
      required: true

    - name: totalAmount
      type: BigDecimal
      required: true
      positive: true

    - name: createdAt
      type: Instant
      generatedOnCreate: true

  operations:
    - create
    - get by id
    - paginated search
    - patch
    - delete

  filters:
    - customerReference
    - status

output: generated/orders-api
```

A complete example is available at [`examples/orders.yaml`](examples/orders.yaml).

## Generate the complete microservice

The CLI is agent-vendor neutral. Configure a coding-agent CLI that accepts a task through standard input:

```bash
export AGENT_RULES_AGENT_COMMAND='your-agent-cli --non-interactive'
```

Then run:

```bash
agent-rules create examples/orders.yaml
```

The flow is:

```text
service.yaml
    ↓
Python CLI
    ↓
complete prompt
    ↓
coding agent reads AGENTS.md + docs/
    ↓
generates the Spring Boot microservice
    ↓
static rule validation
    ↓
Maven formatting + tests
    ↓
Docker Compose + SQL Server
    ↓
HTTP + direct SQL persistence verification
```

The agent command can also be provided per invocation:

```bash
agent-rules create examples/orders.yaml \
  --agent-command 'your-agent-cli --non-interactive'
```

or in the YAML under `agent.command`.

## Inspect the generated prompt

To see exactly what the coding agent will receive without generating anything:

```bash
agent-rules prompt examples/orders.yaml
```

The generated prompt contains the project, feature, fields, operations and filters from YAML and tells the agent to read the canonical repository rules. It also requires a reusable, non-destructive `scripts/verify-persistence.sh` for the generated feature.

## Validate a generated project

```bash
agent-rules validate generated/orders-api
```

Static validation checks include:

- Java 21 in `pom.xml`;
- Maven Wrapper;
- SQL Server JDBC driver;
- Flyway SQL Server module;
- absence of H2;
- Dockerfile and Docker Compose;
- SQL Server container;
- Hibernate `ddl-auto: validate`;
- Flyway migrations;
- persistence verification script.

## Full verification

```bash
agent-rules verify generated/orders-api
```

The CLI runs the static checks and then:

```bash
./mvnw spotless:check
./mvnw verify
docker compose config
docker compose up -d --build
docker compose ps -a
./scripts/verify-persistence.sh
```

The generated persistence script must create a unique record through the real HTTP API, verify it directly in SQL Server, recreate the containers without deleting the named volume and verify the same record again through both paths.

For environments without Docker you can still run build verification:

```bash
agent-rules verify generated/orders-api --skip-runtime
```

To generate without automatically verifying:

```bash
agent-rules create examples/orders.yaml --skip-verify
```

## Validate the rules repository itself

```bash
agent-rules validate-rules
```

This catches baseline drift such as reintroducing Java 25 or the old `Maven or Gradle` ambiguity into the canonical documentation.

The included GitHub Actions workflow runs the Python tests and this rules validation on pushes and pull requests.

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

Controllers handle HTTP concerns only. Services own business logic and transaction boundaries. Models contain no JPA annotations. Persistence is isolated in entities/repositories. Explicit mappers connect DTOs, models and entities.

## Generated local stack

New persistent microservices include a complete Docker Compose stack containing the application, SQL Server, health checks, database initialization and a named database volume.

The generated project must be startable with:

```bash
docker compose up -d --build
```

Persistence is not considered verified from unit tests or an API response alone. The rules require a direct SQL Server query before and after container recreation while retaining the named volume.

## Corporate SQL Server environments

Local Docker authentication is intentionally simpler than a corporate deployment. Existing named datasources, TLS/encryption, NTLM/integrated security, trust stores, persistence units, Hikari settings and external secret groups must be preserved rather than replaced with local defaults. Secret values must never be copied into source control.

See [`docs/database.md`](docs/database.md) for the full contract.

## Quality gate

Generated Maven projects must provide the wrapper and pass:

```bash
./mvnw spotless:check && ./mvnw verify
```

For persistent services, that build gate is followed by the Docker Compose and persistence verification described above.

## Start manually without the CLI

The CLI is the recommended orchestration path, but the rules remain usable directly with any coding agent. A minimal manual task is:

```text
Read AGENTS.md completely and every relevant document under docs/.
Create a complete Spring Boot microservice for the requested feature.
Use repository defaults for Java, build tooling, SQL Server, Flyway, Docker and testing.
Generate everything required to build, run and verify it.
Run formatting, tests and the complete persistence verification before finishing.
```

For a longer standalone generation prompt, see [`START_HERE.md`](START_HERE.md).

## Principle

The YAML says what to build. The repository rules say how to build it. The Python CLI coordinates the coding agent and verification. The generated Spring Boot application remains a normal, independent project.

## License

No license file is currently included. Add one before redistribution or external contributions if needed.
