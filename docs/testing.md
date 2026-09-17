# Testing

This document is the canonical source of truth for automated tests, CI behavior, verification states, publication rules, and the containerized runtime acceptance test.

Use JUnit 6, Mockito, and AssertJ. Tests mirror the feature layer packages.

## What to test

- Service unit test: always expected for business logic.
- Controller test (`@WebMvcTest`): when validation, status mapping, security, or response shape is meaningful.
- Repository test (`@DataJpaTest`): for custom `@Query`, Specifications, or persistence behavior you own; not for plain Spring Data CRUD.
- Cucumber test: when the user explicitly requests BDD/acceptance scenarios or when executable business examples materially improve the feature.

Automated unit and MVC tests do not replace the runtime acceptance test for a newly generated persistent microservice.

In service unit tests, `@InjectMocks` targets `<Feature>ServiceImpl`, not the interface. Everywhere else, mock the service interface.

## Spring Boot 4 test dependencies

Spring Boot 4 modularized several test starters. Do not assume `spring-boot-starter-test` alone provides every MVC test type.

Before adding or keeping `@WebMvcTest` tests in a Spring Boot 4 project:

1. Inspect the resolved Spring Boot version.
2. Verify that the MVC test starter used by that version is present. For Spring Boot 4, include `org.springframework.boot:spring-boot-starter-webmvc-test` with test scope when MVC slice tests require it.
3. Compile the complete test source set before declaring the workflow correct.
4. Treat missing test-only packages as a dependency/configuration problem first; do not rewrite working tests to bypass a missing starter.

## Mockito and overloaded repository methods

When mocking or verifying overloaded repository methods, use typed matchers that identify the intended overload.

```java
verify(repository, never()).delete(any(CryptocurrencyEntity.class));
```

Avoid raw `any()` where overload resolution can become ambiguous.

## Coverage

Generated Maven services must configure JaCoCo to enforce at least 80 percent line coverage of application logic. Generated OpenAPI sources may be excluded from the coverage calculation because they are build output rather than handwritten application logic.

Do not weaken the threshold merely to make CI pass. Add or improve meaningful tests when coverage is below 80 percent.

## Verification states

Use these states precisely:

```text
GENERATED
  -> production clean package
COMPILED
  -> verify with configured tests and JaCoCo >= 80%
TESTED
  -> required CI jobs pass on the exact revision
VERIFIED
  -> runtime acceptance for a new persistent service
RUNTIME_VERIFIED
```

A passing compile job proves production code and generated API sources compile. A passing verification/coverage job proves configured tests pass and the JaCoCo threshold is satisfied. Spotless is a code-formatting tool and does not determine whether a revision may be merged.

Never report persistence as verified unless the runtime acceptance test actually passed.

## Recommended local verification

Before reporting a Java change complete, run when the execution environment permits:

```bash
./mvnw --batch-mode --no-transfer-progress spotless:apply
./mvnw --batch-mode --no-transfer-progress -Dmaven.test.skip=true clean package
./mvnw --batch-mode --no-transfer-progress verify
```

`spotless:apply` formats code but is not a publication or merge gate. Do not remove Spotless from projects that use the standard stack merely to avoid formatting work.

## Publication policy and constrained environments

Publication to the default branch is allowed when the repository's required CI jobs pass on the exact revision being published.

For the default generated workflow, the required evidence is:

- production compile/package succeeds using the Maven Wrapper;
- API-first validation/generation succeeds as part of the Maven lifecycle when configured;
- `verify` succeeds;
- configured tests pass;
- JaCoCo reports at least 80 percent line coverage of application logic.

Failure to run Spotless locally does not by itself require an `unverified/` branch and does not block merge when the required CI jobs above pass.

If the required CI jobs cannot execute or fail, do not describe the revision as verified. A user may explicitly request publication of such work to a non-default `unverified/` or `wip/` branch, but the default branch should receive only revisions whose required CI jobs pass.

## CI workflow rules

For Java 21 + Maven projects, the default GitHub Actions workflow uses the Maven Wrapper with two independently diagnosable jobs:

1. `compile`: production compile/package only;
2. `coverage`: Maven `verify`, which runs configured tests and enforces JaCoCo >= 80 percent.

Spotless is not a mandatory GitHub Actions gate.

Reference shape:

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

  coverage:
    name: Tests and coverage
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v5
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - run: ./mvnw --batch-mode --no-transfer-progress verify
```

Before considering the workflow finished, verify:

- workflow YAML is syntactically valid;
- Java version matches the project;
- selected action versions are current and not deprecated in runner logs;
- production compile/package passes;
- API-first validation/generation participates in the Maven lifecycle when configured;
- generated production sources compile;
- complete test sources compile;
- configured tests pass;
- Cucumber executes at least one scenario when configured;
- JaCoCo enforces at least 80 percent line coverage of application logic;
- CI uses the Maven Wrapper rather than silently switching to global Maven.

## Cucumber with Maven and JUnit Platform

When Cucumber is requested, configure its JUnit Platform engine explicitly, keep feature files under `src/test/resources`, step definitions under `src/test/java`, and prove Maven discovers and executes at least one scenario before considering the workflow complete.

Remember that selecting one suite still compiles the complete test source set.

## Test style

- snake_case test method names ending in `_ok` / `_ko` where useful;
- `// given`, `// when`, `// then` comments;
- AssertJ assertions;
- `assertAll(...)` for related assertions when useful;
- `var` for test locals;
- no `final` on local variables;
- field injection with test annotations is acceptable;
- keep reflection, loops, and branching business logic out of test bodies;
- use private helpers when setup becomes repetitive;
- test one behavior per test.

For each service, cover at least:

- happy path;
- find-or-404 behavior;
- guard clauses that reject before persistence;
- typed `never()` verification where overloaded methods make raw matchers ambiguous;
- PATCH semantics when PATCH is part of the contract.

## Containerized runtime acceptance test

For a newly generated persistent microservice, or when the user asks to prove that it runs and persists data, perform this after the automated compile and verification gates:

1. Run `docker compose config` and require success.
2. Start the complete stack with `docker compose up -d --build`.
3. Wait for healthy application and database containers and require one-shot initializers to exit with code `0`.
4. Create a uniquely identifiable record through the real public HTTP endpoint and capture status, body, identifier, and unique value.
5. Query that row directly using the database container's native client.
6. Run `docker compose down` without `-v`, then start again with `docker compose up -d`.
7. Retrieve the same identifier through the API and require matching values.
8. Query the database directly again and require the same row to exist.
9. Check final container health and identify the named volume that retained the data.

The runtime test passes only when production compilation, automated tests, coverage, Flyway, container health, API creation, direct database verification, restart retrieval, second database verification, and named-volume persistence all succeed.

Never use `docker compose down -v`, `docker volume rm`, or equivalent destructive volume deletion during persistence verification unless the user explicitly requests a clean reset.

## Documentation consistency rule

Mandatory CI gates, stack defaults, publication gates, and verification-state definitions must have exactly one canonical definition in the appropriate source document.

Other documents must link to the canonical definition instead of contradicting it.

When changing a canonical rule, search the repository for duplicated or contradictory wording and update it in the same change.
