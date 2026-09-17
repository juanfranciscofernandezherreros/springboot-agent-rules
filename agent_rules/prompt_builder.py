from __future__ import annotations

from pathlib import Path
from typing import Any

from .models import FieldSpec, ServiceSpec


def _format_option(key: str, value: Any) -> str:
    label = {
        "primaryKey": "primary key",
        "generated": "generated",
        "required": "required",
        "max": "max",
        "min": "min",
        "positive": "positive",
        "unique": "unique",
        "default": "default",
        "generatedOnCreate": "generated on create",
        "values": "values",
    }.get(key, key)

    if isinstance(value, bool):
        return label if value else f"{label}=false"
    if isinstance(value, list):
        return f"{label}=[{', '.join(str(item) for item in value)}]"
    return f"{label}={value}"


def _format_field(field: FieldSpec) -> str:
    options = ", ".join(_format_option(key, value) for key, value in field.options.items())
    suffix = f", {options}" if options else ""
    return f"- {field.name}: {field.type}{suffix}"


def build_prompt(spec: ServiceSpec, output_dir: str | Path) -> str:
    fields = "\n".join(_format_field(item) for item in spec.feature.fields)
    operations = "\n".join(f"- {item}" for item in spec.feature.operations)
    filters = "\n".join(f"- {item}" for item in spec.feature.filters) or "- none"

    return f"""Read `AGENTS.md` completely and every relevant document under `docs/` before generating code.
Persistence work must follow `docs/database.md` and testing must follow `docs/testing.md`.

Create a complete Spring Boot microservice in:

{Path(output_dir).as_posix()}

Project:
- name: {spec.project.name}
- group: {spec.project.group}
- artifact: {spec.project.name}
- base package: {spec.project.package}

Feature:
- name: {spec.feature.name}
- base path: {spec.feature.base_path}

Fields:
{fields}

Operations:
{operations}

Search filters:
{filters}

Treat the YAML specification as the source of truth for domain requirements. Do not invent enum values,
relationships, uniqueness constraints, defaults, validations, filters, endpoints or other business rules
that are not explicitly present in the specification or already fixed by the repository rules.

Use the repository defaults for Java, Maven Wrapper, Microsoft SQL Server, Flyway,
Docker Compose, architecture, formatting and testing. Do not substitute another
database or build tool unless this specification explicitly requests it.

Generate every file required to build and run the service, including the Maven
Wrapper, application configuration, SQL Server-compatible Flyway migrations,
Dockerfile, .dockerignore, Docker Compose stack, tests and documentation required
by the repository rules.

Also generate `scripts/verify-persistence.sh`. It must be non-destructive and
repeatable. The script must:
1. wait for the application and SQL Server health checks;
2. create a uniquely identifiable record through the public HTTP API;
3. verify that record directly in SQL Server;
4. run `docker compose down` without `-v`;
5. start the stack again without deleting the named volume;
6. verify the same record through both the API and SQL Server;
7. print the persistent volume name and concrete verification evidence.
It must never run `docker compose down -v` or delete a volume.

Run formatting and the complete test suite and fix failures.
Then start the complete Docker Compose stack and run the persistence verification.
Do not consider the task complete until the project builds successfully and every
required verification step passes.
"""
