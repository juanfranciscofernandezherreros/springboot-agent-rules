# Mappers

Pick one mapper strategy per project/team and stay consistent.

## Static mappers

Default convention for this guide:

- `<Feature>Mapper`: DTO ↔ model, used by the controller.
- `<Feature>EntityMapper`: model ↔ entity, used by the service.

Make mapper classes `public final` with a private constructor:

```java
public final class UserMapper {

    private UserMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }
}
```

Mapper methods:

- are static;
- have direction-obvious names (`toModel`, `toEntity`, `toDto`, `fromCreateDto`, `fromUpdateDto`);
- guard against null at the top when null is a supported input;
- assign built/mapped results to a local variable before returning;
- perform no I/O;
- never call services or repositories.

## MapStruct alternative

MapStruct is acceptable when chosen consistently across the project. Use `@Mapper(componentModel = "spring")` and explicit `@Mapping` declarations for non-matching fields.
