# Testing

Use JUnit 6, Mockito, and AssertJ.

Tests mirror the feature layer packages.

## What to test

- Service unit test: always expected for business logic.
- Controller test (`@WebMvcTest`): when validation, status mapping, security, or response shape is meaningful.
- Repository test (`@DataJpaTest`): for custom `@Query`, Specifications, or persistence behavior you own; not for plain Spring Data CRUD.
- Cucumber test: when the user explicitly requests BDD/acceptance scenarios or when executable business examples materially improve the feature. Keep Cucumber focused on behavior; do not duplicate every unit test as a feature scenario.

Automated unit and MVC tests do not replace a runtime acceptance test for a newly generated persistent microservice. The acceptance test must exercise the packaged application, network path, Flyway migrations, JDBC driver, selected database engine, and persistent Docker volume together.

In service unit tests, `@InjectMocks` targets `<Feature>ServiceImpl`, not the interface. Everywhere else, mock the service interface.

## Spring Boot 4 test dependencies

Spring Boot 4 modularized several test starters. Do not assume that `spring-boot-starter-test` alone provides every MVC test type used by the codebase.

Before adding or keeping `@WebMvcTest` tests in a Spring Boot 4 project:

1. Inspect the resolved Spring Boot version.
2. Verify that the MVC test starter used by that version is present. For Spring Boot 4, include `org.springframework.boot:spring-boot-starter-webmvc-test` with test scope when MVC slice tests require it.
3. Compile the complete test source set before declaring the CI workflow correct. Running only one selected test still invokes Maven test compilation for all test classes.
4. Treat missing test-only packages as a dependency/configuration problem first; do not rewrite working tests just to bypass the missing starter.

## Mockito and overloaded repository methods

Spring Data APIs may add overloaded methods between framework versions. Untyped Mockito matchers can then become ambiguous at compile time.

When mocking or verifying overloaded repository methods, always use a typed matcher that identifies the intended overload. For example:

```java
verify(repository, never()).delete(any(CryptocurrencyEntity.class));
```

Avoid this when more than one `delete(...)` overload is visible:

```java
verify(repository, never()).delete(any());
```

Apply the same rule to `save`, `find`, custom repository methods, or any API where overload resolution can become ambiguous after a dependency upgrade.

## Mandatory finalization sequence

For every generated or modified Maven project, run these checks in order before commit, push, pull request creation, or reporting completion:

```bash
./mvnw --batch-mode --no-transfer-progress spotless:apply
./mvnw --batch-mode --no-transfer-progress spotless:check
./mvnw --batch-mode --no-transfer-progress -Dmaven.test.skip=true clean package
./mvnw --batch-mode --no-transfer-progress verify
./mvnw --batch-mode --no-transfer-progress spotless:check verify
```

Rules:

- Every command above must exit with code `0`.
- If `spotless:apply` changes files, rerun all later checks against the formatted sources.
- If any source, test, dependency, build, or formatting file changes after a successful check, rerun the affected sequence; when in doubt, rerun all five commands.
- Never commit or push generated code after only writing files. The quality gate is part of generation.
- Never report a project as buildable, tested, verified, ready, or complete if a required command was not executed successfully.
- If the execution environment lacks Java, Maven Wrapper prerequisites, Docker, network access, or another required runtime, state exactly which verification could not be performed. Do not substitute an assumption for execution evidence.

The compile-only command deliberately uses `-Dmaven.test.skip=true`: it must compile/package production sources without compiling tests. This isolates production compilation failures from test compilation failures.

The final `spotless:check verify` command is the default CI parity gate. When the repository's GitHub Actions workflow uses a different Maven Wrapper command, execute that exact command locally as an additional mandatory gate before publishing.

## Cucumber with Maven and JUnit Platform

When adding Cucumber to a Maven project, configure it explicitly instead of assuming the existing JUnit setup is sufficient.

Use test-scoped dependencies equivalent to:

```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite</artifactId>
    <scope>test</scope>
</dependency>
```

Create a JUnit Platform suite class that selects the Cucumber engine, points at the feature resources, and configures the glue package. Keep feature files under `src/test/resources` and step definitions under `src/test/java`.

Before wiring CI, prove locally that the selected Cucumber suite runs through Maven, for example:

```bash
./mvnw -Dtest=CucumberTest test
```

Remember that this command still compiles every test source file. Existing JUnit tests must therefore compile cleanly even when only the Cucumber suite is executed.

## CI workflow rules

When a generated or modified project is expected to use GitHub Actions, create a workflow under `.github/workflows/` and validate the exact commands locally before committing it.

For Java 21 + Maven projects, prefer separate jobs for compilation/package verification and Cucumber when the user asks for both. A reference shape is:

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

permissions:
  contents: read

jobs:
  compile:
    name: Compile
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v5
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - run: ./mvnw --batch-mode --no-transfer-progress -Dmaven.test.skip=true clean package

  quality:
    name: Quality gate
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v5
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - run: ./mvnw --batch-mode --no-transfer-progress spotless:check verify
```

Use the Maven Wrapper when the repository provides it. Do not silently replace it with a globally installed `mvn` command in generated workflows.

For a compile-only job, use `-Dmaven.test.skip=true` when the intention is to skip both test execution and test compilation. `-DskipTests` skips execution but still compiles test sources, which can make a supposedly compile-only job fail because of unrelated test compilation errors.

Do not make CI run `spotless:apply` merely to hide unformatted code. Formatting must be applied before code is committed; CI should reject dirty formatting with `spotless:check`.

Do not consider a workflow finished until you have checked all of the following:

- the workflow YAML is syntactically valid;
- the Java version matches the project;
- the selected action versions are current and not already deprecated in runner logs;
- the compile/package command passes;
- the complete test source set compiles;
- all configured tests pass;
- the final CI parity command passes;
- the Cucumber suite is discovered and executes at least one scenario when Cucumber is configured;
- existing unit/MVC tests remain compilable;
- Maven dependency changes required by Spring Boot 4 test modularization are present;
- Mockito matchers used with overloaded framework APIs are explicitly typed;
- CI commands use the same wrapper/build conventions as local development.

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
- guard clauses that reject before persistence, including typed `verify(repo, never()).save(any(EntityType.class))` / `delete(any(EntityType.class))` when overloaded methods make raw `any()` ambiguous;
- PATCH semantics for partial updates when PATCH is part of the feature contract.

## Containerized runtime acceptance test

When generating a new persistent microservice, or when the user asks to prove that it runs and persists data, perform this test after the automated quality gate:

1. Run `docker compose config` and require success.
2. Start the complete stack with `docker compose up -d --build`.
3. Wait for healthy application and database containers and require every one-shot initializer to exit with code `0`.
4. Use `curl` to create a uniquely identifiable record through the real HTTP endpoint. Capture the HTTP status, response body, generated identifier, and unique value.
5. Use the database container's native command-line client to select that row from the actual table.
6. Run `docker compose down` without `-v`, then start the stack again with `docker compose up -d`.
7. Use `curl` to retrieve the same record by identifier and require the expected success status and field values.
8. Query the database table again and require the same row to exist.
9. Check final container health and identify the named database volume that retained the data.

The test passes only when all of the following are true:

- formatting passes;
- production compilation/package passes;
- the complete automated test suite passes;
- the exact CI parity command passes;
- Flyway applies or validates all migrations successfully;
- the application and database containers are healthy;
- the create request returns the documented success status;
- the first direct database query finds the row;
- the API returns the same record after container recreation;
- the second direct database query finds the row after container recreation;
- the named volume was retained throughout the restart.

Use bounded timeouts when polling health or invoking `curl`; a hung process is not a passing test. On failure, include the relevant HTTP response or container logs rather than reporting only that a command failed.

Leave the stack running when the user asked to start or expose the microservice. Otherwise, follow the user's requested final state and state clearly whether the containers remain running. Never delete persistent volumes without explicit authorization.
