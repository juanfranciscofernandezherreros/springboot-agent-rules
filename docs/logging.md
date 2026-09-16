# Logging

Use Lombok `@Slf4j`; do not create logger instances manually.

Levels:

- `INFO`: significant successful actions worth tracing.
- `WARN` / `ERROR`: failures and abnormal situations.

Use placeholders, never string concatenation:

```java
log.info("[TASK] - ACTION: createTask: taskId: {}", taskId);
```

Include useful trace identifiers such as request IDs or user IDs when available, but never log secrets or sensitive data.

Keep application logging in the service/application layer. Domain exceptions should not be logged at the throw site when the global exception handler already logs them.
