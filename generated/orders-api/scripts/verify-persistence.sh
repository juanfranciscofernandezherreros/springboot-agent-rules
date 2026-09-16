#!/usr/bin/env bash
set -euo pipefail

app_port="${APP_PORT:-8080}"
sql_password="${SQLSERVER_SA_PASSWORD:-LocalPassw0rd!}"
reference="verify-$(date +%s)"
payload="{\"customerReference\":\"${reference}\",\"status\":\"PENDING\",\"totalAmount\":19.99}"

wait_for_health() {
  local service="$1"
  for attempt in $(seq 1 30); do
    status="$(docker compose ps --format json "$service" | grep -o '"Health":"[^"]*"' | head -1 || true)"
    if [[ "$status" == '"Health":"healthy"' ]]; then
      return 0
    fi
    sleep 2
  done
  docker compose ps -a
  docker compose logs "$service"
  return 1
}

wait_for_health sqlserver
wait_for_health app

response="$(curl --fail-with-body --max-time 20 -sS -X POST "http://localhost:${app_port}/orders" -H 'Content-Type: application/json' -d "$payload")"
id="$(printf '%s' "$response" | sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p')"
test -n "$id"
echo "POST /orders: $response"

query="SELECT id, customer_reference, status, total_amount FROM orders WHERE id = ${id};"
MSYS_NO_PATHCONV=1 docker compose exec -T sqlserver /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P "$sql_password" -d orders -Q "$query"

docker compose down
docker compose up -d
wait_for_health sqlserver
wait_for_health app

retrieved="$(curl --fail-with-body --max-time 20 -sS "http://localhost:${app_port}/orders/${id}")"
echo "GET /orders/${id}: $retrieved"
printf '%s' "$retrieved" | grep -q "$reference"
MSYS_NO_PATHCONV=1 docker compose exec -T sqlserver /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P "$sql_password" -d orders -Q "$query"
docker volume ls --format '{{.Name}}' | grep 'orders-sqlserver-data'
