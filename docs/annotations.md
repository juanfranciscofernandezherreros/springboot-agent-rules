# Annotations

## Lombok

Use Lombok for boilerplate:

- `@RequiredArgsConstructor` for constructor injection.
- `@Slf4j` for logging.
- `@Builder(setterPrefix = "with")` when a builder is useful.
- Avoid `@Data`; use `@Getter` and `@Setter` explicitly.

Model POJOs commonly use:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
```

Entities may use the same Lombok annotations plus JPA annotations, but should not contain business behaviour.

## Spring

- `@RestController` on web controllers.
- `@GetMapping`, `@PostMapping`, `@PatchMapping`, `@DeleteMapping` at method level.
- `@Service` only on `<Feature>ServiceImpl`; the interface remains annotation-free.
- Do not write `@Repository` implementations for standard Spring Data repositories.
- `@Component` for generic Spring beans.
- `@Configuration` for configuration classes.
- Constructor injection in production code; field injection is acceptable only in tests.
- Use `@ConfigurationProperties` for 3 or more related configuration values; smaller cases may use `@Value`.
- Keep `@Transactional` at class level on service classes.
- Use `@Validated` for method/class validation where needed.
- Use `@RequestBody @Valid` for validated write DTOs.
- Use `@PreAuthorize` at controller level when method-level Spring Security is enabled.
- Keep dependencies acyclic. Do not use `@Order` to hide dependency design problems.
