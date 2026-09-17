# Testing

This document is the canonical source of truth for automated tests, CI behavior, verification states, the mandatory Maven finalization sequence, publication rules, and the containerized runtime acceptance test.

Other documents may reference these rules but must not copy, shorten, reorder, or redefine the mandatory finalization sequence.

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

## Verification states

Use these states precisely:

```text
GENERATED
  -> spotless:apply + spotless:check
FORMATTED
  -> production clean package
COMPILED
  -> verify
TESTED
  -> complete mandatory Maven finalization sequence
VERIFIED
  -> runtime acceptance for a new persistent service
RUNTIME_VERIFIED
```

Never report a stronger state than the highest gate that actually passed on the exact revision being discussed.

Examples:

- If production `clean package` passes but tests were not run, say `COMPILED`, not `TESTED`.
- If unit/MVC tests pass but the mandatory Maven finalization sequence was not completed, do not say `VERIFIED`, `ready`, or `complete`.
- A passing compile-only CI job proves the committed revision compiles; it does not prove tests, formatting, coverage, or runtime persistence.
- If Maven verification passes but Docker acceptance could not run, do not say persistence was verified.

## Mandatory finalization sequence

For every generated or modified Maven project, run these commands in this exact order before commit, push, pull request creation, or reporting completion:

```bash
./mvnw --batch-mode --no-transfer-progress spotless:apply
./mvnw --batch-mode --no-transfer-progress spotless:check
./mvnw --batch-mode --no-transfer-progress -Dmaven.test.skip=true clean package
./mvnw --batch-mode --no-transfer-progress verify
./mvnw --batch-mode --no-transfer-progress spotless:check verify
```

This sequence is canonical. Do not shorten it to `spotless:check && verify`, do not reorder it, and do not substitute CI execution for a locally executable pre-publication gate.

Rules:

- Every command above must exit with code `0`.
- If `spotless:apply` changes files, all later checks must run against the formatted files.
- If source, test, dependency, build, formatter, or workflow configuration changes after a successful gate, rerun the affected checks; when uncertain, rerun all five commands.
- The compile-only command intentionally uses `-Dmaven.test.skip=true` so test sources are not compiled.
- The last command is the final local quality gate. CI is not required to repeat it.
- If the repository workflow uses another Maven Wrapper command and the project explicitly requires parity with that workflow, that exact command is an additional pre-publication gate.
- Never report a project as buildable, tested, verified, ready, CI-ready, or complete unless the corresponding gate actually passed.

## Publication policy and constrained environments

An exact revision that has not completed the mandatory finalization sequence is `UNVERIFIED`.

If the execution environment cannot run the target repository's Maven Wrapper or another required finalization dependency:

- do not push generated or modified Java code to the default branch;
- do not open a pull request that is described as verified or ready;
- do not rely on GitHub Actions as a substitute for formatter execution on the exact revision;
- do not guess the output of `spotless:apply`;
- do not weaken or remove `spotless:check` from the canonical local finalization sequence;
- state exactly which verification could not run.

If the user explicitly insists on publishing despite the missing verification, publication is allowed only to a clearly named non-default branch such as `unverified/<description>` or `wip/<description>`. The commit and response must clearly state which gates were not executed. A user instruction to publish does not convert an unverified revision into a verified one and does not authorize an unverified push to the default branch.

## CI workflow rules

CI is a verifier of committed production compilation, not a formatter and not a replacement for the canonical pre-publication sequence.

For Java 21 + Maven projects, the default GitHub Actions workflow should use the Maven Wrapper and run a compile/package-only job. It must validate and generate API-first sources through the normal Maven lifecycle when configured. `spotless:check`, `verify`, tests, Cucumber, and JaCoCo are not mandatory CI jobs by default; they remain part of the canonical local finalization sequence and may be added to CI when the project or user explicitly requires them.

A reference shape is:

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
```

Do not make CI run `spotless:apply` merely to hide unformatted code. Formatting belongs to the canonical local finalization sequence.

Before considering the default workflow finished, verify:

- workflow YAML is syntactically valid;
- Java version matches the project;
- selected action versions are current and not deprecated in runner logs;
- the production compile/package job passes;
- API-first validation/generation participates in that Maven lifecycle when configured;
- generated production sources compile;
- CI uses the Maven Wrapper rather than silently switching to global Maven.

Tests, Cucumber discovery, JaCoCo coverage, and Spotless remain verification requirements when configured, but their proof comes from the mandatory local finalization sequence unless CI explicitly includes those gates.

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

For a newly generated persistent microservice, or when the user asks to prove that it runs and persists data, perform this after the mandatory Maven finalization sequence:

1. Run `docker compose config` and require success.
2. Start the complete stack with `docker compose up -d --build`.
3. Wait for healthy application and database containers and require one-shot initializers to exit with code `0`.
4. Create a uniquely identifiable record through the real public HTTP endpoint and capture status, body, identifier, and unique value.
5. Query that row directly using the database container's native client.
6. Run `docker compose down` without `-v`, then start again with `docker compose up -d`.
7. Retrieve the same identifier through the API and require matching values.
8. Query the database directly again and require the same row to exist.
9. Check final container health and identify the named volume that retained the data.

The runtime test passes only when formatting, production compilation, automated tests, the final local quality gate, Flyway, container health, API creation, direct database verification, restart retrieval, second database verification, and named-volume persistence all succeed.

Never use `docker compose down -v`, `docker volume rm`, or equivalent destructive volume deletion during persistence verification unless the user explicitly requests a clean reset.

## Documentation consistency rule

Mandatory command sequences, stack defaults, publication gates, and verification-state definitions must have exactly one canonical definition in the appropriate source document.

Other documents must link to the canonical definition instead of copying it.

When changing a canonical rule, search the repository for duplicated or contradictory wording and update it in the same change.
