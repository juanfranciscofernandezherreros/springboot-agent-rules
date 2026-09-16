# Python CLI

The Python CLI turns a declarative YAML service specification into a complete agent task and can orchestrate generation and verification of the resulting Spring Boot microservice.

## Responsibility split

- YAML specification: what to build — project identity, feature, fields, operations and filters.
- `AGENTS.md` and `docs/`: how to build it — Java 21, Maven Wrapper, SQL Server, architecture, migrations, Docker and testing rules.
- Python CLI: orchestration — render the prompt, invoke a coding-agent CLI, validate the generated project and execute verification.
- Coding agent: writes and fixes the actual Spring Boot project.

The CLI stays vendor-neutral. The configured agent command must accept the generated prompt on standard input and must be able to edit files under the rules repository working directory.

## Commands

```bash
agent-rules prompt examples/orders.yaml
agent-rules create examples/orders.yaml
agent-rules validate generated/orders-api
agent-rules verify generated/orders-api
agent-rules validate-rules
```

`create` resolves the coding-agent command in this order:

1. `--agent-command`;
2. `agent.command` in the YAML specification;
3. `AGENT_RULES_AGENT_COMMAND` environment variable.

For example:

```bash
export AGENT_RULES_AGENT_COMMAND='your-agent-cli --non-interactive'
agent-rules create examples/orders.yaml
```

The agent command is intentionally not hardcoded so the rules repository can be used with different coding agents.

## Verification contract

A generated persistent service must include `scripts/verify-persistence.sh`. The generated script is feature-specific and therefore knows which endpoint, payload and SQL table to verify. It must create a unique record through HTTP, verify the row directly in SQL Server, recreate containers without deleting the volume, and verify the same data again.

The CLI first performs static checks, then runs:

```bash
./mvnw spotless:check
./mvnw verify
docker compose config
docker compose up -d --build
docker compose ps -a
./scripts/verify-persistence.sh
```

Use `--skip-runtime` when Docker/SQL Server verification is intentionally unavailable, or `--skip-verify` on `create` when only generation is desired.
