# Spring Boot Agent Rules

A practical, opinionated ruleset for AI coding agents that generate and maintain Spring Boot backends with consistent architecture, naming, testing, and code style.

The repository combines two things:

1. **Agent instructions and engineering standards** in [`AGENTS.md`](AGENTS.md) and [`docs/`](docs/).
2. **A reference Task API** showing those conventions in a real Spring Boot project.

The goal is simple: give an AI coding agent enough explicit context to produce Spring Boot code that looks like it belongs to the same codebase every time.

## Why this repository exists

AI coding agents can generate valid Spring Boot code in many different ways. Without project-level rules, that often leads to inconsistent layering, naming, annotations, exception handling, mappings, and tests.

This repository defines those decisions up front so the agent can focus on implementing features instead of inventing conventions.

## What the rules cover

The root [`AGENTS.md`](AGENTS.md) file is the entry point. It instructs the coding agent to inspect the relevant standards before generating or modifying code.

The detailed rules live under [`docs/`](docs/):

| Document | Purpose |
| --- | --- |
| [`java-style.md`](docs/java-style.md) | Java style and formatting conventions |
| [`annotations.md`](docs/annotations.md) | Annotation usage and placement |
| [`layered-architecture.md`](docs/layered-architecture.md) | Feature-oriented layered architecture |
| [`controllers.md`](docs/controllers.md) | REST controller conventions |
| [`mappers.md`](docs/mappers.md) | DTO, model, and entity mapping rules |
| [`exceptions.md`](docs/exceptions.md) | Application and API error handling |
| [`testing.md`](docs/testing.md) | Unit and integration testing conventions |
| [`logging.md`](docs/logging.md) | Logging conventions |

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

The main architectural rules include:

- Controllers handle HTTP concerns only.
- Services expose an interface plus a single implementation.
- Business and persistence concerns stay out of controllers.
- Models are plain Java objects without JPA annotations.
- Persistence is isolated in entity classes and repositories.
- DTO ↔ model and model ↔ entity transformations use explicit mappers.
- Transaction boundaries belong in the service layer.
- Existing project patterns take precedence over introducing new abstractions.

## Using the rules with an AI coding agent

Open this repository, or copy `AGENTS.md` and `docs/` into your Spring Boot project, then give your coding agent an explicit instruction to read them before making changes.

Example prompt:

```text
Read AGENTS.md completely and read every relevant file under docs/
before generating or modifying code.

Create a new Customer CRUD feature following the repository rules.

Requirements:
- REST endpoints under /customers
- PostgreSQL persistence
- Bean Validation
- Flyway migration
- Unit tests for the service
- MVC tests for the controller

Do not introduce new dependencies unless required.
Run formatting and tests when finished.
```

For a complete project-generation example, see [`START_HERE.md`](START_HERE.md).

## Reference application

The repository currently includes a small Task management API that demonstrates the conventions in practice.

### Current sample stack

- Spring Boot 4.0.0
- Java 21
- Maven Wrapper
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway
- Lombok
- JUnit Platform
- Cucumber
- H2 for tests
- Spotless with Palantir Java Format

> [!NOTE]
> `AGENTS.md` currently defines **Java 25** as the target for generated projects, while the included reference application's `pom.xml` is configured for **Java 21**. Treat the ruleset as the source of truth when generating a new project unless you intentionally choose another Java version.

## Running the reference application

### Prerequisites

- Java 21+
- Docker / Docker Compose

The Maven Wrapper is included, so a separate Maven installation is not required.

### Start PostgreSQL

```bash
docker compose up -d
```

The provided Compose configuration starts PostgreSQL on port `5432` with these defaults:

```text
Database: tasks
Username: tasks
Password: tasks
```

### Run the application

macOS / Linux:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
mvnw.cmd spring-boot:run
```

By default the application connects to:

```text
jdbc:postgresql://localhost:5432/tasks
```

The connection can be overridden with environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

## Task API

The reference application exposes the following endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/tasks` | Create a task |
| `GET` | `/tasks/{id}` | Get a task by ID |
| `GET` | `/tasks/search` | Search and paginate tasks |
| `PATCH` | `/tasks/{id}` | Partially update a task |
| `DELETE` | `/tasks/{id}` | Delete a task |

The search endpoint supports optional `title` and `completed` filters plus Spring Data pagination parameters.

Example:

```bash
curl "http://localhost:8080/tasks/search?title=spring&completed=false&page=0&size=20"
```

## Development commands

Run tests:

```bash
./mvnw test
```

Check formatting and the full Maven verification lifecycle:

```bash
./mvnw verify
```

Apply Spotless formatting:

```bash
./mvnw spotless:apply
```

## Repository structure

```text
.
├── AGENTS.md              # Root instructions for AI coding agents
├── START_HERE.md          # Example project-generation prompt
├── docs/                  # Detailed engineering rules
├── src/                   # Reference Spring Boot application
├── compose.yaml           # Local PostgreSQL
├── pom.xml                # Maven build
├── mvnw                   # Maven Wrapper for Unix-like systems
└── mvnw.cmd               # Maven Wrapper for Windows
```

## Recommended workflow

When using these rules in a project:

1. Give the coding agent the feature request.
2. Require it to read `AGENTS.md` and the relevant documents under `docs/`.
3. Ask it to inspect existing code before creating new files.
4. Keep each change focused on the requested feature.
5. Run formatting and the relevant tests after the implementation.
6. Review generated migrations and public API changes before merging.

## Customizing the rules

This repository is intentionally opinionated. Fork it or copy the rules into your own project and adapt decisions such as:

- Java version
- package layout
- DTO naming
- testing strategy
- mapper approach
- transaction boundaries
- logging policy
- formatting rules

The important part is not using these exact conventions; it is making your conventions explicit enough that both humans and coding agents can follow them consistently.

## License

No license file is currently included in the repository. Add one before redistributing or accepting external contributions if needed.
