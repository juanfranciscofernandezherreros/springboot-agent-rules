# Java Style

This document is the canonical source for Java source style and formatter behavior. The complete verification and publication sequence is defined only in [`testing.md`](testing.md).

Use 4-space indentation, 120-character lines, IntelliJ IDEA's default Java style, and UTF-8.

Use blank lines to separate logical blocks. Keep a blank line before and after a `return`, loop, or stream chain unless it is the first or last line in its block.

Enforce formatting automatically with Spotless and Palantir Java Format for generated projects unless an existing project already establishes another formatter.

## Formatting behavior

After generating or modifying Java source:

- run `spotless:apply` before `spotless:check` as part of the canonical finalization sequence in `docs/testing.md`;
- treat every formatter-produced change as part of the implementation;
- never treat hand formatting as equivalent to executing Spotless;
- after any subsequent Java change, rerun the affected finalization gates;
- run `spotless:check` in CI, not `spotless:apply`;
- never remove or weaken formatter checks to make CI green.

This document intentionally does not duplicate the full Maven command sequence. Follow [`testing.md`](testing.md) exactly.

## Formatter execution capability guard

Formatter execution and publication behavior are governed by the `UNVERIFIED` publication policy in [`testing.md`](testing.md).

In particular, if the current environment cannot execute the target repository's formatter on the exact revision, the agent must not guess what the formatter would change or describe that revision as formatted or ready.

When Git is available after formatter execution, run `git diff --check` and ensure formatter-produced changes are included before publication.

## Variables and parameters

- Do not use `final` on method parameters or local variables.
- Keep `final` only on constructor-injected fields when Lombok requires it.
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
