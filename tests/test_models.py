from pathlib import Path

import pytest

from agent_rules.models import SpecError, load_spec


def test_load_spec(tmp_path: Path):
    spec_file = tmp_path / "service.yaml"
    spec_file.write_text(
        """
project:
  name: customers-api
  group: com.acme
  package: com.acme.customers
feature:
  name: Customer
  basePath: /customers
  fields:
    - name: id
      type: Long
      generated: true
  operations:
    - create
""",
        encoding="utf-8",
    )

    spec = load_spec(spec_file)

    assert spec.project.name == "customers-api"
    assert spec.feature.base_path == "/customers"
    assert spec.feature.fields[0].options["generated"] is True


def test_enum_like_field_requires_values(tmp_path: Path):
    spec_file = tmp_path / "service.yaml"
    spec_file.write_text(
        """
project:
  name: cryptocurrencies-api
  group: com.acme
  package: com.acme.cryptocurrencies
feature:
  name: Cryptocurrency
  basePath: /cryptocurrencies
  fields:
    - name: status
      type: CryptocurrencyStatus
      required: true
  operations:
    - create
""",
        encoding="utf-8",
    )

    with pytest.raises(SpecError, match="requires a non-empty 'values' list"):
        load_spec(spec_file)


def test_enum_like_field_accepts_values(tmp_path: Path):
    spec_file = tmp_path / "service.yaml"
    spec_file.write_text(
        """
project:
  name: cryptocurrencies-api
  group: com.acme
  package: com.acme.cryptocurrencies
feature:
  name: Cryptocurrency
  basePath: /cryptocurrencies
  fields:
    - name: status
      type: CryptocurrencyStatus
      required: true
      values:
        - ACTIVE
        - INACTIVE
  operations:
    - create
""",
        encoding="utf-8",
    )

    spec = load_spec(spec_file)

    assert spec.feature.fields[0].options["values"] == ["ACTIVE", "INACTIVE"]


def test_filters_must_reference_existing_fields(tmp_path: Path):
    spec_file = tmp_path / "service.yaml"
    spec_file.write_text(
        """
project:
  name: customers-api
  group: com.acme
  package: com.acme.customers
feature:
  name: Customer
  basePath: /customers
  fields:
    - name: id
      type: Long
  operations:
    - paginated search
  filters:
    - missing
""",
        encoding="utf-8",
    )

    with pytest.raises(SpecError, match="Unknown filter field"):
        load_spec(spec_file)


def test_duplicate_fields_are_rejected(tmp_path: Path):
    spec_file = tmp_path / "service.yaml"
    spec_file.write_text(
        """
project:
  name: customers-api
  group: com.acme
  package: com.acme.customers
feature:
  name: Customer
  basePath: /customers
  fields:
    - name: id
      type: Long
    - name: id
      type: String
  operations:
    - create
""",
        encoding="utf-8",
    )

    with pytest.raises(SpecError, match="Duplicate field name"):
        load_spec(spec_file)
