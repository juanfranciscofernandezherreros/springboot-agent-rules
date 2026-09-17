# Java Style

Use 4-space indentation, 120-character lines, IntelliJ IDEA's default Java style, and UTF-8.

Use blank lines to separate logical blocks. Keep a blank line before and after a `return`, loop, or stream chain unless it is the first or last line in its block.

Enforce formatting automatically. Prefer Spotless with Palantir Java Format.

## Mandatory formatting sequence

After generating or modifying Java source, always run the formatter before checking formatting:

```bash
./mvnw --batch-mode --no-transfer-progress spotless:apply
./mvnw --batch-mode --no-transfer-progress spotless:check
```

`spotless:check` verifies formatting but does not fix it. Never rely on CI to format generated code.

If `spotless:apply` modifies files, those modifications are part of the implementation and must be included before compilation, tests, commit, push, or pull request creation.

After any subsequent Java change, rerun `spotless:apply` and `spotless:check`.

Run `spotless:check` in CI together with the test suite. CI is a verifier, not a formatter: do not replace `spotless:check` with `spotless:apply` in CI merely to make a pipeline pass.

A formatting failure means the implementation is not ready to publish.

## Variables and parameters

- Do not use `final` on method parameters or local variables.
- Keep `final` only on Lombok constructor-injected fields when `@RequiredArgsConstructor` needs it.
- Use `var` in controllers and tests.
- Use explicit types in services, mappers, and the rest of production code.

## General rules

- Prefer no more than 3 parameters on a method or constructor. If several values belong together, use a record or value object.
- Prefer immutability.
- Avoid magic numbers and magic strings; give them meaningful names.
- Check nullness and emptiness before operating on strings or collections when the contract allows null/empty values.
- Prefer unchecked exceptions over `throws` declarations for domain/application failures.
- Avoid comments except for cron expressions, regex explanations, TODOs, and `given/when/then` test separators.
- Use `@Override` whenever overriding a method.
- For simple null checks, prefer `value == null` / `value != null` over `Objects.isNull`.
- Extract complex boolean expressions into named booleans.
- Prefer early returns over deeply nested `if/else` blocks.
- Do not use wildcard imports.
- Do not add Javadocs unless explicitly requested or required by an external/public API contract.
