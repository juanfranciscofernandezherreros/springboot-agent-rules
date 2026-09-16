from agent_rules.models import FeatureSpec, FieldSpec, ProjectSpec, ServiceSpec
from agent_rules.prompt_builder import build_prompt


def test_prompt_contains_service_contract():
    spec = ServiceSpec(
        project=ProjectSpec("orders-api", "com.acme", "com.acme.orders"),
        feature=FeatureSpec(
            name="Order",
            base_path="/orders",
            fields=(
                FieldSpec("id", "Long", {"primaryKey": True, "generated": True}),
                FieldSpec("totalAmount", "BigDecimal", {"required": True, "positive": True}),
            ),
            operations=("create", "get by id"),
            filters=("totalAmount",),
        ),
    )

    prompt = build_prompt(spec, "generated/orders-api")

    assert "generated/orders-api" in prompt
    assert "- id: Long, primary key, generated" in prompt
    assert "- totalAmount: BigDecimal, required, positive" in prompt
    assert "Microsoft SQL Server" in prompt
    assert "scripts/verify-persistence.sh" in prompt
    assert "docker compose down` without `-v`" in prompt
