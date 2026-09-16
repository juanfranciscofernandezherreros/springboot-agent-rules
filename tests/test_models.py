from pathlib import Path

from agent_rules.models import load_spec


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
