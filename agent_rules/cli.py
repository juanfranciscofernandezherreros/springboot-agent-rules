from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

from .models import SpecError, load_spec
from .prompt_builder import build_prompt
from .runner import CommandError, run_agent, verify_project
from .validators import failures, render_checks, validate_project, validate_rules


def find_rules_root(explicit: str | None = None) -> Path:
    candidates: list[Path] = []
    if explicit:
        candidates.append(Path(explicit))
    if os.environ.get("AGENT_RULES_ROOT"):
        candidates.append(Path(os.environ["AGENT_RULES_ROOT"]))
    candidates.extend([Path.cwd(), Path(__file__).resolve().parents[1]])

    for candidate in candidates:
        resolved = candidate.resolve()
        if (resolved / "AGENTS.md").is_file() and (resolved / "docs").is_dir():
            return resolved
    raise SpecError("Could not locate rules root. Run from the repository or use --rules-root.")


def resolve_output(root: Path, configured: str | None, project_name: str) -> Path:
    raw = Path(configured or f"generated/{project_name}")
    return raw.resolve() if raw.is_absolute() else (root / raw).resolve()


def print_and_code(checks) -> int:
    print(render_checks(checks))
    return 1 if failures(checks) else 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="agent-rules",
        description="Generate and verify Spring Boot microservices using this repository's agent rules.",
    )
    parser.add_argument("--rules-root", help="Path to the springboot-agent-rules repository")

    sub = parser.add_subparsers(dest="command", required=True)

    prompt = sub.add_parser("prompt", help="Render the agent prompt from a YAML specification")
    prompt.add_argument("spec")
    prompt.add_argument("--output")

    create = sub.add_parser("create", help="Generate a microservice through a coding-agent CLI")
    create.add_argument("spec")
    create.add_argument("--output")
    create.add_argument("--agent-command")
    create.add_argument("--skip-verify", action="store_true")
    create.add_argument("--skip-runtime", action="store_true")

    validate = sub.add_parser("validate", help="Validate a generated Spring Boot project")
    validate.add_argument("project_dir")

    verify = sub.add_parser("verify", help="Validate, build and runtime-test a generated project")
    verify.add_argument("project_dir")
    verify.add_argument("--skip-runtime", action="store_true")

    sub.add_parser("validate-rules", help="Check this rules repository for baseline drift")
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)

    try:
        rules_root = find_rules_root(args.rules_root)

        if args.command == "validate-rules":
            return print_and_code(validate_rules(rules_root))

        if args.command == "validate":
            project_dir = Path(args.project_dir).resolve()
            return print_and_code(validate_project(project_dir))

        if args.command == "verify":
            verify_project(Path(args.project_dir).resolve(), runtime=not args.skip_runtime)
            return 0

        spec = load_spec(args.spec)
        output_dir = resolve_output(rules_root, args.output or spec.output, spec.project.name)
        prompt = build_prompt(spec, output_dir)

        if args.command == "prompt":
            print(prompt)
            return 0

        agent_command = (
            args.agent_command
            or spec.agent.command
            or os.environ.get("AGENT_RULES_AGENT_COMMAND")
        )
        if not agent_command:
            parser.error(
                "create requires --agent-command, agent.command in the spec, "
                "or AGENT_RULES_AGENT_COMMAND"
            )

        output_dir.parent.mkdir(parents=True, exist_ok=True)
        run_agent(agent_command, prompt, rules_root, output_dir)

        checks = validate_project(output_dir)
        print(render_checks(checks))
        failed = failures(checks)
        if failed:
            print(f"Generated project failed {len(failed)} static validation check(s).", file=sys.stderr)
            return 2

        if not args.skip_verify:
            verify_project(output_dir, runtime=not args.skip_runtime)

        print(f"SUCCESS: project created at {output_dir}")
        return 0

    except (SpecError, CommandError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
