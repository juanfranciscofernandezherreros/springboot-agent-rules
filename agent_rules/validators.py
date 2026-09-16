from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class Check:
    name: str
    ok: bool
    detail: str


def validate_rules(root: Path) -> list[Check]:
    required_files = (
        "AGENTS.md",
        "README.md",
        "START_HERE.md",
        "docs/database.md",
        "docs/testing.md",
    )
    checks: list[Check] = []

    for relative in required_files:
        checks.append(Check(f"rules file {relative}", (root / relative).is_file(), relative))

    readable = [root / item for item in required_files if (root / item).is_file()]
    text = "\n".join(path.read_text(encoding="utf-8", errors="ignore") for path in readable)
    policy_text = "\n".join(
        (root / item).read_text(encoding="utf-8", errors="ignore")
        for item in ("AGENTS.md", "START_HERE.md", "docs/database.md", "docs/testing.md")
        if (root / item).is_file()
    )

    checks.extend(
        [
            Check("Java 21 default", "Java 21" in text, "rules mention Java 21"),
            Check("SQL Server default", "SQL Server" in text, "rules mention SQL Server"),
            Check("Maven Wrapper default", "Maven Wrapper" in text, "rules mention Maven Wrapper"),
            Check("no Java 25 drift", "Java 25" not in policy_text, "Java 25 must not appear in canonical rules"),
            Check(
                "no Maven-or-Gradle drift",
                "Maven or Gradle" not in policy_text,
                "canonical stack must not reintroduce Maven-or-Gradle ambiguity",
            ),
        ]
    )
    return checks


def validate_project(root: Path) -> list[Check]:
    checks: list[Check] = []

    pom = root / "pom.xml"
    pom_text = pom.read_text(encoding="utf-8", errors="ignore") if pom.exists() else ""

    checks.extend(
        [
            Check("pom.xml", pom.is_file(), str(pom)),
            Check("Maven Wrapper", (root / "mvnw").is_file(), "mvnw"),
            Check("Java 21", "<java.version>21</java.version>" in pom_text, "pom.xml"),
            Check("SQL Server JDBC", "mssql-jdbc" in pom_text, "pom.xml"),
            Check("Flyway SQL Server", "flyway-sqlserver" in pom_text, "pom.xml"),
            Check("Spring Boot Flyway starter", "spring-boot-starter-flyway" in pom_text, "pom.xml"),
            Check("H2 absent", "com.h2database" not in pom_text and "<artifactId>h2</artifactId>" not in pom_text, "pom.xml"),
            Check("Dockerfile", (root / "Dockerfile").is_file(), "Dockerfile"),
        ]
    )

    compose_names = ("compose.yaml", "compose.yml", "docker-compose.yaml", "docker-compose.yml")
    compose = next((root / name for name in compose_names if (root / name).is_file()), None)
    compose_text = compose.read_text(encoding="utf-8", errors="ignore") if compose else ""
    checks.extend(
        [
            Check("Docker Compose", compose is not None, str(compose) if compose else "missing"),
            Check(
                "SQL Server container",
                "mcr.microsoft.com/mssql/server" in compose_text,
                str(compose) if compose else "missing compose file",
            ),
        ]
    )

    config_files = (
        "src/main/resources/application.yml",
        "src/main/resources/application.yaml",
        "src/main/resources/application.properties",
    )
    config_text = "\n".join(
        (root / item).read_text(encoding="utf-8", errors="ignore")
        for item in config_files
        if (root / item).is_file()
    )
    checks.append(
        Check(
            "Hibernate validates schema",
            "ddl-auto: validate" in config_text
            or "ddl-auto=validate" in config_text
            or "ddl-auto:validate" in config_text,
            "application configuration",
        )
    )

    migration_dir = root / "src/main/resources/db/migration"
    migrations = list(migration_dir.glob("V*__*.sql")) if migration_dir.is_dir() else []
    checks.append(Check("Flyway migrations", bool(migrations), str(migration_dir)))

    verify_script = root / "scripts/verify-persistence.sh"
    checks.append(Check("persistence verification script", verify_script.is_file(), str(verify_script)))

    return checks


def failures(checks: list[Check]) -> list[Check]:
    return [check for check in checks if not check.ok]


def render_checks(checks: list[Check]) -> str:
    return "\n".join(
        f"{'[OK]' if check.ok else '[FAIL]'} {check.name}: {check.detail}" for check in checks
    )
