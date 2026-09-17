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

For interactive work, do not require the user to provide a complete contract manually. Run the interactive discovery process defined below and ask only for missing decisions that materially affect the public API, database schema, validation, business behavior, persistence semantics, security, or integration contracts.

When the task must proceed without clarification, choose only the smallest reversible implementation necessary and document every assumption prominently as an assumption, not as a discovered requirement.

Never invent regulated, financial, security, compliance, or accounting behavior such as fees, balances, settlement guarantees, fraud rules, transfer limits, idempotency guarantees, authorization rules, or state transitions unless explicitly defined.

## Interactive domain contract discovery

When the user requests generation of a new microservice, feature, API, or persistent domain and the domain contract is incomplete, conduct an interactive requirements-discovery process before generating production code.

Do not ask the user to manually provide a complete domain contract upfront. Derive everything already available from the request and existing project sources, then ask only for unresolved decisions that materially affect:

- the public API;
- database schema;
- validation;
- business behavior;
- persistence semantics;
- security;
- integration contracts.

### Discovery workflow

Follow these phases in order. Skip any phase whose answers are already established by higher-priority sources.

### Phase 1 — Understand the feature

Determine:

- feature or domain name;
- purpose of the microservice or feature;
- primary business resource;
- expected consumers of the API when relevant to the contract.

If these are unclear, ask concise business-level questions such as:

- What is the main resource managed by this service?
- What should clients be able to do with it?

Do not ask implementation questions during this phase.

### Phase 2 — Discover the data model

For each main resource, determine:

- fields;
- field types;
- required vs optional fields;
- identifier;
- identifier generation strategy;
- uniqueness constraints;
- enumerations;
- relationships;
- generated values;
- default values.

Ask about business concepts rather than Java, JPA, or SQL implementation whenever possible.

Prefer:

> What information do you need to store for each investment fund?

Instead of:

> Which JPA fields should `FundEntity` contain?

Infer obvious technical types when the mapping is unambiguous, for example:

- date -> `LocalDate`;
- monetary decimal -> `BigDecimal`;
- yes/no value -> `Boolean`.

If different technical mappings would materially affect the contract, ask the user to choose or provide the missing business semantics.

### Phase 3 — Discover validation and business rules

For each field or operation, determine whether there are:

- minimum or maximum values;
- maximum lengths;
- allowed values;
- uniqueness rules;
- required combinations;
- prohibited combinations;
- state transitions;
- business invariants.

Ask only questions relevant to the declared domain.

Never invent financial, regulatory, accounting, compliance, security, or legal rules. For regulated or financial domains, explicitly ask whether additional business rules exist before generating them.

Example:

> Does the risk level have a fixed range, such as 1-7, or should it be stored without that validation?

### Phase 4 — Discover API operations

Determine which operations are required. Ask about:

- create;
- retrieve by identifier;
- list/search;
- update;
- partial update;
- delete.

Do not assume full CRUD is required.

When list/search exists, determine:

- pagination;
- filterable fields;
- sortable fields;
- default sorting when required by the contract.

Do not invent search filters or sorting rules.

### Phase 5 — Discover update and deletion semantics

If updates are supported, determine:

- PUT, PATCH, or both;
- immutable fields;
- whether omitted PATCH fields remain unchanged;
- whether an explicit `null` clears optional values.

If deletion is supported, determine:

- physical deletion;
- soft deletion;
- deletion forbidden under specified states or conditions.

Never assume soft delete.

### Phase 6 — Discover errors

Determine domain-specific error conditions that affect the public API.

The following technical behavior may follow established project conventions without additional questioning:

- resource not found -> standard not-found mechanism;
- Bean Validation failures -> standard validation mechanism;
- malformed requests -> standard framework handling.

Ask only when business-specific errors or mappings exist.

### Phase 7 — Discover persistence constraints

For new persistent services, the database engine follows repository defaults unless explicitly overridden.

Ask only about persistence decisions that belong to the business or external contract, such as:

- schema name when externally mandated;
- required table name when externally mandated;
- uniqueness constraints;
- foreign-key relationships;
- required indexes when dictated by known query requirements;
- integration with an existing database.

Do not ask the user to choose JPA, Flyway, SQL Server, Maven, Docker, or other technical defaults already established by this repository unless the user explicitly wants to override them.

### Phase 8 — Discover security and audit requirements

Determine whether the API requires:

- authentication;
- authorization;
- role restrictions;
- ownership rules;
- audit history;
- created-by or updated-by information.

If none are requested, do not invent them. Absence of security requirements does not authorize the agent to invent an authentication model.

### Phase 9 — Present the proposed contract

Once enough information has been collected, present a concise proposed domain contract before generating production code.

Include, when applicable:

- feature name;
- API base path;
- resource fields and types;
- required/optional status;
- validations;
- identifiers;
- uniqueness constraints;
- operations;
- search filters;
- pagination and sorting;
- update semantics;
- deletion semantics;
- relationships;
- relevant error cases;
- security requirements;
- audit requirements;
- persistence requirements.

Classify every contract item as one of:

- `USER REQUIREMENT`;
- `REPOSITORY DEFAULT`;
- `TECHNICAL ASSUMPTION`.

A technical assumption must be minimal, reversible, and must not introduce business behavior.

### Phase 10 — Resolve only blocking ambiguities

If the proposed contract still contains an ambiguity that could materially change the public API, database schema, business rules, security, integration behavior, or persistence semantics, ask the user to resolve it before generating production code.

Do not ask about minor implementation choices already governed by repository conventions.

### Question batching

Do not interrogate the user with the complete checklist at once.

Ask questions in small coherent groups, normally between one and five questions. Order them so answers to earlier questions can eliminate unnecessary later questions.

Prefer multiple-choice questions when there are a small number of meaningful alternatives.

Example:

> How should deletion work?
>
> 1. Physical deletion from SQL Server
> 2. Soft delete using an active/deleted flag
> 3. Deletion should not be supported

### Known information must not be asked again

Before asking any question, inspect:

1. the user's current request;
2. previous messages in the conversation;
3. existing API specifications;
4. existing code;
5. existing migrations;
6. existing tests;
7. project documentation.

Never ask for information that is already clearly established by one of these sources.

### Progressive contract

Maintain a progressive contract throughout the interview.

After each user response:

1. incorporate the new requirements;
2. determine which required contract elements remain unresolved;
3. ask only the highest-priority unresolved questions.

Do not restart the questionnaire from the beginning.

### Exit condition

The discovery phase is complete when no unresolved question remains that could materially change:

- public API shape;
- database schema;
- validation or business rules;
- persistence semantics;
- security behavior;
- integration contracts.

At that point, present the proposed contract and proceed according to the user's instruction.

Do not delay generation merely because optional implementation details are unspecified when repository conventions already define them.

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

## API-first contracts

When the user explicitly requests API-first generation, the OpenAPI document is the public-contract source of truth. The build must validate that document and generate the server API interface from it; the HTTP controller implements that generated interface. Do not maintain a handwritten duplicate interface or independently declared HTTP mappings that can diverge from the generated contract.

The specification must declare every public request and response field, operation, path parameter, query parameter, validation constraint, and documented success or error status exposed by the service. Regenerate the server contract during the Maven lifecycle and compile the generated source set.

### Generated-source dependency verification

API-first generation is not complete when the generator merely writes source files. The generated source set must compile as part of the normal production build.

After selecting or changing the OpenAPI Generator version, generator name, library, or config options:

1. Generate the server source set.
2. Inspect the generated imports and referenced framework types before finalizing dependencies.
3. Add only the compile/runtime dependencies actually required by the generated output and the chosen configuration.
4. Verify those dependency versions are compatible with the resolved Spring Boot version and with each other; do not assume that every OpenAPI Generator, Springdoc, Swagger annotations, nullable/Jackson, or Spring Framework combination is interchangeable.
5. Do not add Springdoc, Swagger annotations, nullable helpers, or similar libraries merely because they are common in generated projects. Add them only when the generated source or an explicit application requirement needs them.
6. Prefer generator configuration that avoids unnecessary generated framework dependencies when it still satisfies the requested contract and established project conventions.
7. Run the production compile/package gate from `docs/testing.md` against the generated source set. Missing generated-code packages or annotations are a build/dependency configuration failure, not a reason to hand-edit generated files or duplicate the generated API interface.

When generated imports change after a generator/configuration upgrade, reassess the dependency set instead of preserving stale dependencies blindly.
