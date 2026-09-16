# Backend — Spring Boot

Standards: [`docs/java-style.md`](docs/java-style.md) · [`docs/annotations.md`](docs/annotations.md) ·
[`docs/layered-architecture.md`](docs/layered-architecture.md) · [`docs/controllers.md`](docs/controllers.md) ·
[`docs/mappers.md`](docs/mappers.md) · [`docs/exceptions.md`](docs/exceptions.md) ·
[`docs/testing.md`](docs/testing.md) · [`docs/logging.md`](docs/logging.md)

## Agent workflow

Before modifying or generating code:

1. Read this file completely.
2. Read the relevant files under `docs/`.
3. Inspect existing project conventions before creating new files.
4. Do not introduce new dependencies unless required by the requested feature.
5. Follow the architecture documented here even if another Spring convention would also work.
6. Keep changes focused on the requested task.
7. Do not change public APIs, database schemas, or architectural conventions unless explicitly requested.
8. After changes, run formatting and the relevant tests.
9. Prefer existing patterns over inventing new abstractions.
10. If generating a project from scratch, create all files necessary for it to build and run.

## Stack

Java 25 · Spring Boot 4.x · Maven or Gradle · Spring Data JPA · Lombok · JUnit 6 + Mockito + AssertJ.

## Feature package layout

One package per feature, split into layer subpackages:

```text
com/<company>/<app>/<feature>/
  controller/   <Feature>Controller     @RestController @RequestMapping("/<feature>")
  service/      <Feature>Service        interface — the feature's public surface
                <Feature>ServiceImpl    @Service, class-level @Transactional — the one impl
  repository/   <Feature>Repository     interface extends JpaRepository<<Feature>Entity, Id>
  model/        <Feature>, enums        plain POJO (Lombok), no jakarta.persistence imports
  entity/       <Feature>Entity         @Entity only, no logic
  dto/          Create/Update/Response records
  mapper/       <Feature>Mapper         static — DTO ↔ model
                <Feature>EntityMapper   static — model ↔ entity
```

Not every feature needs every file. Add a layer only when it actually carries weight.

## Gotchas

- The service is an interface `<Feature>Service` plus one `@Service` implementation `<Feature>ServiceImpl`.
- Inject and mock the interface. In the service unit test, `@InjectMocks` targets `<Feature>ServiceImpl`.
- The repository is a plain Spring Data interface. "Find or 404" is a service concern.
- `model/` holds plain POJOs with no JPA imports; `entity/` holds persistence classes only.
- No `final` on method parameters or local variables. Keep `final` only on constructor-injected fields when Lombok requires it.
- Use `var` in controllers and tests; explicit types in services, mappers, and the rest of production code.
- No existence checks or business logic in controllers.
- `@Transactional` belongs at class level on service implementations; use `readOnly = true` for read-only services/paths where applicable.
