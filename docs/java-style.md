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

## Formatter execution capability guard

An agent must not publish generated or modified Java code to the target/default branch unless it has actually executed the repository Maven Wrapper formatter successfully against the exact files being published.

This rule applies especially to tool-constrained environments such as GitHub-only connectors where files can be written but local Maven commands cannot be executed.

- Having generated code that appears formatted is not evidence that Spotless will accept it.
- Manually approximating Palantir Java Format is not a substitute for `spotless:apply`.
- Reading a Spotless diff from a previous CI run is not a substitute for executing the formatter on the current revision.
- If the environment cannot run `./mvnw ... spotless:apply`, do not push the Java change to the default branch and do not report it as ready.
- If publication is explicitly required but execution is unavailable, report the verification blocker instead of creating an unverified default-branch commit.
- After `spotless:apply`, run `git diff --check` when Git is available and ensure no formatter-produced changes remain uncommitted before publication.

Generated projects that use these rules must keep Spotless configured in `pom.xml`; removing or bypassing the formatter to make CI green is prohibited.

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
