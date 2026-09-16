# Start here

This folder is not a Spring Boot application. It is a ruleset for an AI coding agent.

Open this folder/repository in your coding agent and ask it to generate the project you want.

Example prompt:

```text
Read AGENTS.md and every referenced file under docs/ before generating code.

Create a complete Spring Boot application from scratch with:
- Java 25
- Spring Boot 4.x
- Maven
- PostgreSQL
- Spring Data JPA
- Lombok
- Bean Validation
- Flyway
- JUnit 6
- Mockito
- AssertJ
- Spotless with Palantir Java Format

Project:
- name: orders-api
- group: com.acme
- artifact: orders-api
- base package: com.acme.orders

Initial feature: Order CRUD.

Generate everything required to build and run the application, including:
- pom.xml
- Maven Wrapper
- main Spring Boot application class
- application.yml
- Docker Compose for PostgreSQL
- Flyway migrations
- feature packages and layers
- global exception handling
- unit tests

Follow AGENTS.md strictly.
When finished, run formatting and tests and fix any failures.
```

The agent should treat `AGENTS.md` as the root instruction file and open the relevant files under `docs/` before implementing each part.
