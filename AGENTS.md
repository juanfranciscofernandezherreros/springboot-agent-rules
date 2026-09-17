# Backend — Spring Boot

Standards: [`docs/domain-contract.md`](docs/domain-contract.md) · [`docs/java-style.md`](docs/java-style.md) · [`docs/annotations.md`](docs/annotations.md) · [`docs/layered-architecture.md`](docs/layered-architecture.md) · [`docs/controllers.md`](docs/controllers.md) · [`docs/mappers.md`](docs/mappers.md) · [`docs/exceptions.md`](docs/exceptions.md) · [`docs/testing.md`](docs/testing.md) · [`docs/logging.md`](docs/logging.md) · [`docs/database.md`](docs/database.md)

## Rule precedence

This file is the root instruction source for agent behavior.

When instructions overlap, use this precedence:

1. `AGENTS.md` defines mandatory agent workflow and precedence.
2. The relevant document under `docs/` defines the canonical subject-specific rule.
3. `README.md` and `START_HERE.md` are explanatory only and must not redefine mandatory rules, command sequences, publication gates, or stack defaults.

If two documents conflict, follow the higher-precedence source and fix the lower-precedence document in the same change when possible.

Mandatory command sequences, stack defaults, publication gates, and verification-state definitions must have exactly one canonical definition. Other documents must link to that definition rather than copying it.

## Agent workflow

Before modifying or generating code:

1. Read this file completely.
2. Read every relevant canonical file under `docs/`.
3. Persistence work always requires `docs/database.md`.
4. New feature/API generation always requires `docs/domain-contract.md`.
5. Verification, CI, finalization, and publication behavior always follow `docs/testing.md` exactly.
6. Inspect existing project conventions before creating new files.
7. Establish the domain contract from the user's request and existing project sources before generating production code.
8. When the requested feature contract is incomplete, do not require the user to provide a fully specified contract manually. Run the interactive contract discovery process defined in `docs/domain-contract.md`, asking only for requirements that cannot be derived from the request or existing project sources.
9. Do not silently invent business fields, states, transitions, validation rules, endpoints, identifiers, financial rules, security behavior, persistence semantics, datasource settings, or schema rules.
10. Do not introduce dependencies unless required by the requested feature, selected database engine, selected web stack, generated source set, or established project conventions.
11. Keep changes focused on the requested task.
12. Do not change public APIs, database schemas, datasource configuration, or architectural conventions unless explicitly requested.
13. Prefer existing patterns over inventing new abstractions.
14. If generating a project from scratch, create all files necessary for it to build and run.
15. New generated projects use Java 21 unless the user explicitly requests another supported Java version.
16. New Spring Boot 4 MVC services use `spring-boot-starter-webmvc` unless an existing project already establishes another supported web stack.
17. New persistent projects use Microsoft SQL Server by default unless explicitly overridden.
18. Existing projects preserve their already configured datasource and database engine unless the user explicitly requests migration or replacement.
19. A new persistent microservice includes a production-style `Dockerfile`, `.dockerignore`, and Docker Compose stack for the application and SQL Server with health-aware dependencies and persistent storage.
20. Before commit, push, pull request creation, or reporting completion, execute the canonical finalization sequence from `docs/testing.md` on the exact revision.
21. Do not copy, shorten, reorder, or partially substitute that sequence in this file or elsewhere.
22. GitHub Actions uses the Maven Wrapper and follows the default compile/package-only CI policy from `docs/testing.md` unless the project or user explicitly requires additional CI gates.
23. CI verifies committed production compilation; it does not repair formatting and does not replace executable local formatter, test, coverage, or verification gates.
24. Never report a stronger verification state than the highest state actually achieved on the exact revision. Use the state model in `docs/testing.md`.
25. For a new persistent microservice, automated tests do not replace the runtime acceptance and persistence test defined in `docs/testing.md` and `docs/database.md`.
26. Report concrete verification evidence and exact blockers. Never claim compiled, tested, persistence-verified, CI-ready, verified, or complete without the corresponding execution evidence.

## Publication guard

Publication behavior for unverified revisions is defined canonically in `docs/testing.md`.

In summary:

- an exact revision that has not completed the mandatory finalization sequence is `UNVERIFIED`;
- an unverified Java revision must not be pushed directly to the default branch;
- a later instruction to "push anyway" does not convert the revision into a verified one and does not authorize bypassing the default-branch guard;
- when the user explicitly insists on publication despite missing execution capability, use a clearly named non-default branch such as `unverified/<description>` or `wip/<description>` and disclose the missing gates;
- do not guess what `spotless:apply` would change;
- do not weaken the canonical local formatter/finalization gates to manufacture a passing verification state.

## Stack

For newly generated services, unless explicitly overridden or an existing project establishes another supported convention:

- Java 21;
- Spring Boot 4.x;
- Maven Wrapper;
- Spring MVC via `spring-boot-starter-webmvc`;
- Spring Data JPA;
- Bean Validation;
- Lombok;
- JUnit 6 + Mockito + AssertJ;
- Spotless with Palantir Java Format;
- Microsoft SQL Server for persistence;
- Flyway for schema evolution.

For explicitly versioned third-party integrations, verify compatibility with the resolved Spring Boot version rather than assuming all 4.x combinations are interchangeable.

## Domain contract rules

`docs/domain-contract.md` is the source of truth for determining feature inputs and preventing silent invention of business requirements.

Before generating a feature, establish as applicable:

- feature name and API base path;
- operations and HTTP methods;
- request/response fields and Java/wire types;
- required and optional fields;
- validation constraints;
- identifiers and generation strategy;
- enums, allowed values and state transitions;
- business invariants;
- uniqueness constraints;
- relationships and foreign keys;
- generated/default values;
- supported CRUD/search behavior;
- pagination, filters and sorting;
- idempotency requirements;
- error cases and HTTP status mapping;
- authentication/authorization and audit requirements when present;
- indexes and migration requirements.

Do not present implementation assumptions as established business requirements. Never invent regulated, financial, accounting, compliance, or security behavior.

## Database and datasource rules

`docs/database.md` is the source of truth for datasource, SQL Server, Docker Compose, Flyway, multi-datasource, secret handling, engine-specific mappings, and environment rules.

Before creating or changing persistence code:

1. Inspect the existing datasource configuration, JDBC dependencies, Flyway modules, JPA configuration, persistence units, transaction managers, and migration scripts.
2. Reuse an existing configured database engine unless the request explicitly asks to migrate or replace it.
3. Never invent credentials, production hosts, schemas, datasource names, authentication modes, trust stores, or pool sizes.
4. Never hardcode production credentials or secret values.
5. Database migrations must use SQL compatible with the selected database engine.
6. Flyway owns deployed schema evolution; Hibernate validates deployed schemas unless an established test profile intentionally differs.
7. When database-specific behavior matters, test against the selected engine rather than assuming H2 is equivalent.

## Feature package layout

One package per feature, split into layer subpackages:

```text
com/<company>/<app>/<feature>/
  controller/   <Feature>Controller
  service/      <Feature>Service
                <Feature>ServiceImpl
  repository/   <Feature>Repository
  model/        <Feature>, enums
  entity/       <Feature>Entity
  dto/          Create/Update/Response records
  mapper/       <Feature>Mapper
                <Feature>EntityMapper
```

Not every feature needs every file. Add a layer only when it carries meaningful responsibility.

## Core architecture rules

- The service is an interface `<Feature>Service` plus one `@Service` implementation `<Feature>ServiceImpl`.
- Inject and mock the interface; `@InjectMocks` targets the implementation in service unit tests.
- The repository is a Spring Data interface. Find-or-404 is a service concern.
- `model/` contains plain domain/in-memory types with no JPA imports.
- `entity/` contains persistence mapping, not business behavior.
- Controllers bind/validate HTTP input, call services, map outputs, and set status codes; they do not access repositories or implement business branching.
- Use explicit DTO/model/entity mappers according to `docs/mappers.md`.
- Use the HTTP mapping annotation that matches the contract, including `@PutMapping` when PUT is part of the API.
- `@Transactional` belongs at class level on service implementations; method-level `@Transactional(readOnly = true)` may intentionally override the class default for reads.
- Use `var` in controllers and tests; explicit production types elsewhere according to `docs/java-style.md`.
- No `final` on method parameters or local variables.
- Spring Boot 4 test dependencies are modular; verify MVC test starter requirements before using `@WebMvcTest`.
- Maven `-DskipTests` still compiles tests; use `-Dmaven.test.skip=true` for a true production compile/package-only job.
- Use typed Mockito matchers when framework overloads make raw `any()` ambiguous.
- When Cucumber is requested, configure its JUnit Platform engine explicitly and prove at least one scenario is discovered and executed.
- Never delete persistent Docker volumes during a persistence verification unless the user explicitly requests destructive reset behavior.

## Documentation consistency rule

When changing a canonical rule, search the repository for duplicated or contradictory wording and update it in the same change.

Do not add a second copy of the canonical Maven finalization sequence outside `docs/testing.md`.
