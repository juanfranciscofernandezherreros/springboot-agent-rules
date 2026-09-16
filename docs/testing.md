# Testing

Use JUnit 6, Mockito, and AssertJ.

Tests mirror the feature layer packages.

## What to test

- Service unit test: always expected for business logic.
- Controller test (`@WebMvcTest`): when validation, status mapping, security, or response shape is meaningful.
- Repository test (`@DataJpaTest`): for custom `@Query`, Specifications, or persistence behavior you own; not for plain Spring Data CRUD.

Automated unit and MVC tests do not replace a runtime acceptance test for a newly generated persistent microservice. The acceptance test must exercise the packaged application, network path, Flyway migrations, JDBC driver, selected database engine, and persistent Docker volume together.

In service unit tests, `@InjectMocks` targets `<Feature>ServiceImpl`, not the interface. Everywhere else, mock the service interface.

For Spring Boot 4.x MVC tests, use `org.springframework.test.context.bean.override.mockito.MockitoBean`; do not use the removed `org.springframework.boot.test.mock.mockito.MockitoBean` package.

## Style

- snake_case test method names ending in `_ok` / `_ko` where useful.
- Use `// given`, `// when`, `// then`.
- Use AssertJ assertions.
- Group related assertions with `assertAll(...)` when multiple mismatches should be reported together.
- Use `var` for locals.
- Do not use `final` on local variables.
- Field injection with `@Mock`, `@InjectMocks`, `@Autowired`, or equivalent test annotations is acceptable.
- Keep reflection, loops, and branching business logic out of test bodies.
- Use private test factory/helper methods when setup becomes repetitive.
- Test one behavior per test.

For each service, cover at least:

- happy path;
- find-or-404 behavior;
- guard clauses that reject before persistence, including `verify(repo, never()).save(any())` where relevant;
- PATCH semantics for partial updates.

Quality gate:

```bash
./mvnw spotless:check && ./mvnw verify
```

## Containerized runtime acceptance test

When generating a new persistent microservice, or when the user asks to prove that it runs and persists data, perform this test after the automated quality gate:

1. Validate the Compose configuration and build the application image.
2. Start the complete stack and wait for healthy application and database containers.
3. Use `curl` to create a record through the real HTTP endpoint. Capture the HTTP status, response body, generated identifier, and a unique value such as an email or external reference.
4. Use the database container's native command-line client to select that row from the actual table.
5. Recreate the containers with `docker compose down` followed by `docker compose up -d`. Do not pass `-v`.
6. Use `curl` to retrieve the same record by identifier and require the expected success status and field values.
7. Query the database table again and require the same row to exist.
8. Check final container health and report the named database volume.

The verifier must poll health with bounded retries both before the create request and after container recreation. A transient connection failure during startup is not success and must be retried or reported with logs.

The test passes only when all of the following are true:

- the normal build and automated tests pass;
- Flyway applies or validates all migrations successfully;
- the application and database containers are healthy;
- the create request returns the documented success status;
- the first direct database query finds the row;
- the API returns the same record after container recreation;
- the second direct database query finds the row after container recreation;
- the named volume was retained throughout the restart.

Use bounded timeouts when polling health or invoking `curl`; a hung process is not a passing test. On failure, include the relevant HTTP response or container logs rather than reporting only that a command failed.

Leave the stack running when the user asked to start or expose the microservice. Otherwise, follow the user's requested final state and state clearly whether the containers remain running. Never delete persistent volumes without explicit authorization.
