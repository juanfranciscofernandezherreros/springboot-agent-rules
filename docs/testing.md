# Testing

Use JUnit 6, Mockito, and AssertJ.

Tests mirror the feature layer packages.

## What to test

- Service unit test: always expected for business logic.
- Controller test (`@WebMvcTest`): when validation, status mapping, security, or response shape is meaningful.
- Repository test (`@DataJpaTest`): for custom `@Query`, Specifications, or persistence behavior you own; not for plain Spring Data CRUD.

In service unit tests, `@InjectMocks` targets `<Feature>ServiceImpl`, not the interface. Everywhere else, mock the service interface.

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
./mvnw spotless:check && ./mvnw test
```
