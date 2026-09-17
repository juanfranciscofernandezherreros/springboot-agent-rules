from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

import yaml


class SpecError(ValueError):
    """Raised when a service specification is invalid."""


@dataclass(frozen=True)
class FieldSpec:
    name: str
    type: str
    options: dict[str, Any] = field(default_factory=dict)


@dataclass(frozen=True)
class ProjectSpec:
    name: str
    group: str
    package: str


@dataclass(frozen=True)
class FeatureSpec:
    name: str
    base_path: str
    fields: tuple[FieldSpec, ...]
    operations: tuple[str, ...]
    filters: tuple[str, ...] = ()


@dataclass(frozen=True)
class AgentSpec:
    command: str | None = None


@dataclass(frozen=True)
class ServiceSpec:
    project: ProjectSpec
    feature: FeatureSpec
    agent: AgentSpec = AgentSpec()
    output: str | None = None


def _required(mapping: dict[str, Any], key: str, context: str) -> Any:
    value = mapping.get(key)
    if value is None or value == "":
        raise SpecError(f"Missing required value '{context}.{key}'")
    return value


def _as_mapping(value: Any, context: str) -> dict[str, Any]:
    if not isinstance(value, dict):
        raise SpecError(f"'{context}' must be a mapping")
    return value


def _looks_like_enum(field_type: str) -> bool:
    return field_type.endswith("Status") or field_type.endswith("Type") or field_type.endswith("Enum")


def _validate_field_semantics(fields: list[FieldSpec], filters: list[str]) -> None:
    field_names = [field.name for field in fields]
    duplicate_names = sorted({name for name in field_names if field_names.count(name) > 1})
    if duplicate_names:
        raise SpecError(f"Duplicate field name(s): {', '.join(duplicate_names)}")

    missing_filters = [name for name in filters if name not in field_names]
    if missing_filters:
        raise SpecError(f"Unknown filter field(s): {', '.join(missing_filters)}")

    for field in fields:
        values = field.options.get("values")
        if _looks_like_enum(field.type):
            if values is None:
                raise SpecError(
                    f"Field '{field.name}' uses enum-like type '{field.type}' and requires a non-empty 'values' list"
                )
            if not isinstance(values, list) or not values or not all(isinstance(item, str) and item for item in values):
                raise SpecError(
                    f"Field '{field.name}' option 'values' must be a non-empty list of strings"
                )

        max_value = field.options.get("max")
        min_value = field.options.get("min")
        if max_value is not None and min_value is not None:
            try:
                if min_value > max_value:
                    raise SpecError(f"Field '{field.name}' has min greater than max")
            except TypeError as exc:
                raise SpecError(f"Field '{field.name}' min/max values must be comparable") from exc


def load_spec(path: str | Path) -> ServiceSpec:
    spec_path = Path(path)
    try:
        raw = yaml.safe_load(spec_path.read_text(encoding="utf-8"))
    except FileNotFoundError as exc:
        raise SpecError(f"Spec file not found: {spec_path}") from exc
    except yaml.YAMLError as exc:
        raise SpecError(f"Invalid YAML in {spec_path}: {exc}") from exc

    root = _as_mapping(raw, "root")
    project_raw = _as_mapping(_required(root, "project", "root"), "project")
    feature_raw = _as_mapping(_required(root, "feature", "root"), "feature")

    project = ProjectSpec(
        name=str(_required(project_raw, "name", "project")),
        group=str(_required(project_raw, "group", "project")),
        package=str(_required(project_raw, "package", "project")),
    )

    fields_raw = _required(feature_raw, "fields", "feature")
    if not isinstance(fields_raw, list) or not fields_raw:
        raise SpecError("'feature.fields' must be a non-empty list")

    parsed_fields: list[FieldSpec] = []
    for index, raw_field in enumerate(fields_raw):
        field_map = _as_mapping(raw_field, f"feature.fields[{index}]")
        name = str(_required(field_map, "name", f"feature.fields[{index}]"))
        field_type = str(_required(field_map, "type", f"feature.fields[{index}]"))
        options = {k: v for k, v in field_map.items() if k not in {"name", "type"}}
        parsed_fields.append(FieldSpec(name=name, type=field_type, options=options))

    operations_raw = _required(feature_raw, "operations", "feature")
    if not isinstance(operations_raw, list) or not operations_raw:
        raise SpecError("'feature.operations' must be a non-empty list")

    filters_raw = feature_raw.get("filters", [])
    if not isinstance(filters_raw, list):
        raise SpecError("'feature.filters' must be a list")
    parsed_filters = [str(item) for item in filters_raw]

    _validate_field_semantics(parsed_fields, parsed_filters)

    feature = FeatureSpec(
        name=str(_required(feature_raw, "name", "feature")),
        base_path=str(_required(feature_raw, "basePath", "feature")),
        fields=tuple(parsed_fields),
        operations=tuple(str(item) for item in operations_raw),
        filters=tuple(parsed_filters),
    )

    agent_raw = root.get("agent") or {}
    agent_map = _as_mapping(agent_raw, "agent")
    agent = AgentSpec(command=str(agent_map["command"]) if agent_map.get("command") else None)

    output = str(root["output"]) if root.get("output") else None
    return ServiceSpec(project=project, feature=feature, agent=agent, output=output)
