# orders-api

Order microservice generated from `examples/orders.yaml`.

Run locally with `docker compose up -d --build`. The API is available at `http://localhost:8080/orders`.

Use `./mvnw spotless:check && ./mvnw verify` for the automated quality gate and
`./scripts/verify-persistence.sh` for the end-to-end SQL Server persistence check.
