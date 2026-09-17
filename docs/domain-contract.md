# Domain Contract

Define the feature contract before generating production code. Do not silently invent business fields, states, validation rules, endpoints, transitions, identifiers, uniqueness rules, relationships, or persistence semantics.

## Source of truth

Determine the domain contract from, in order:

1. the user's explicit request;
2. an existing API specification such as OpenAPI;
3. existing project code and tests;
4. existing database schema and migrations;
5. project documentation and established conventions.

If these sources define the contract, preserve it.

## Missing requirements

When essential domain details are missing, do not present invented assumptions as requirements.

For interactive work, ask for the missing contract when it materially changes the public API, schema, or business behavior. When the task must proceed without clarification, choose only the smallest reversible implementation necessary and document every assumption prominently as an assumption, not as a discovered requirement.

Never invent regulated, financial, security, compliance, or accounting behavior such as fees, balances, settlement guarantees, fraud rules, transfer limits, idempotency guarantees, authorization rules, or state transitions unless explicitly defined.

## Contract checklist

Before generating a persistent feature, establish as applicable:

- feature name and API base path;
- operation names and HTTP methods;
- request and response fields;
- field Java types and wire formats;
- required and optional fields;
- validation constraints;
- identifiers and generation strategy;
- enums and allowed values;
- state transitions and business invariants;
- uniqueness constraints;
- relationships and foreign keys;
- generated/default values;
- supported create/read/update/delete/search operations;
- pagination, filtering and sorting behavior;
- idempotency requirements;
- error cases and HTTP status mapping;
- authentication/authorization requirements when present;
- audit requirements when present;
- database indexes and migration requirements.

## Traceability

Generated README/API documentation must distinguish clearly between:

- requirements provided by the user or existing project;
- conventions inherited from this ruleset;
- implementation assumptions made because requirements were absent.

Do not use wording that makes an assumption sound like an established business rule.
