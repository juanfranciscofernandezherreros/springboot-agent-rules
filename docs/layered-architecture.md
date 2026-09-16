# Layered Architecture

Each feature is one package split into layers.

| Layer | Responsibility | May import | Must not |
|---|---|---|---|
| controller | Bind/validate request, call one service method, map result to DTO, set HTTP status | service, dto, mapper, model | repository/entity; business branching; existence checks |
| service | Business logic, transactions, not-found handling, uniqueness checks, pagination/sort, orchestration | repository, model, mapper, other features' service + dto/model | controllers; another feature's repository/entity |
| repository | Persistence through Spring Data interfaces | entity | handwritten implementation unless explicitly required; business logic |
| model | In-memory representation | Lombok, other model types | JPA, Spring, DTOs |
| entity | Database row | JPA, Lombok | business behaviour; controller exposure |
| dto | Wire shape | minimal dependencies | divergence from API contract |
| mapper | Copy between representations | the two mapped types | service/repository calls; I/O |

Typical request flow:

```text
POST /<feature>
  Controller(Create<Feature>DTO)
    → <Feature>Mapper.toModel(dto)
    → <Feature>Service.create(model)
        → <Feature>EntityMapper.toEntity(model)
        → repository.save(entity)
        → <Feature>EntityMapper.toModel(saved)
    → <Feature>Mapper.toDto(model)
```

A paged read passes `Pageable` from controller to service. The service returns `Page<Model>` and the controller maps it to the API response envelope.

Hard feature boundary: one feature may call another feature's service and use its DTO/model types, but it must not access another feature's repository or entity directly.
