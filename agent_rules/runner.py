from __future__ import annotations

import os
import shlex
import shutil
import subprocess
from pathlib import Path

from .validators import failures, render_checks, validate_project


class CommandError(RuntimeError):
    pass


def _run(command: list[str], cwd: Path, *, stdin: str | None = None) -> None:
    result = subprocess.run(command, cwd=cwd, input=stdin, text=True, check=False)
    if result.returncode != 0:
        raise CommandError(f"Command failed ({result.returncode}): {' '.join(command)}")


def run_agent(command: str, prompt: str, rules_root: Path, output_dir: Path) -> None:
    argv = shlex.split(command)
    if not argv:
        raise CommandError("Agent command is empty")

    env = os.environ.copy()
    env["AGENT_RULES_OUTPUT_DIR"] = str(output_dir)
    result = subprocess.run(
        argv,
        cwd=rules_root,
        input=prompt,
        text=True,
        env=env,
        check=False,
    )
    if result.returncode != 0:
        raise CommandError(f"Agent command failed ({result.returncode}): {command}")


def verify_project(project_dir: Path, *, runtime: bool = True) -> None:
    checks = validate_project(project_dir)
    print(render_checks(checks))
    failed = failures(checks)
    if failed:
        raise CommandError(f"Static project validation failed with {len(failed)} error(s)")

    mvnw = project_dir / "mvnw"
    if os.name != "nt":
        mvnw.chmod(mvnw.stat().st_mode | 0o111)

    _run([str(mvnw), "spotless:check"], project_dir)
    _run([str(mvnw), "verify"], project_dir)

    if not runtime:
        return

    _run(["docker", "compose", "config"], project_dir)
    _run(["docker", "compose", "up", "-d", "--build"], project_dir)
    _run(["docker", "compose", "ps", "-a"], project_dir)

    verify_script = project_dir / "scripts/verify-persistence.sh"
    if os.name != "nt":
        verify_script.chmod(verify_script.stat().st_mode | 0o111)
        _run([str(verify_script)], project_dir)
        return

    git_bash = Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "Git" / "bin" / "bash.exe"
    bash = str(git_bash) if git_bash.is_file() else shutil.which("bash")
    if not bash:
        raise CommandError("A Bash executable with Docker CLI access is required to run scripts/verify-persistence.sh")

    _run([bash, str(verify_script)], project_dir)
