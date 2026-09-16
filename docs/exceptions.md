# Exceptions

Use one domain/application exception type carrying an error enum/code instead of one exception class per failure mode.

Example:

```java
throw new AppException(AppErrorMessage.TASK_NOT_FOUND);
```

Use one global exception handler:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    // @ExceptionHandler methods
}
```

The global handler is responsible for:

- mapping exceptions to HTTP status codes;
- returning a consistent error response shape;
- logging failures once.

Do not add local `@ExceptionHandler` methods inside controllers.

Code that throws a domain exception must not also log the same failure. Log it once in the global handler.
