# Controllers

Controllers are HTTP adapters only:

1. map request DTO to model;
2. call the service;
3. map the result to response DTO;
4. set the appropriate HTTP status.

Do not put business logic, existence checks, repository calls, or local exception handlers in controllers.

Every mapping and service call should be a separate statement:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public TaskDTO createTask(@RequestBody @Valid CreateTaskDTO dto) {
    var task = TaskMapper.toModel(dto);
    var created = taskService.create(task);
    var response = TaskMapper.toDto(created);

    return response;
}
```

Avoid nested calls such as:

```java
return TaskMapper.toDto(taskService.create(TaskMapper.toModel(dto)));
```

## Conventions

- Use `var` for local variables.
- Use one or two filter values as individual `@RequestParam`s.
- For three or more related filters, use a filter object with `@ModelAttribute`.
- Search endpoints should use names like `search...`, not `list...`.
- Create: `201 CREATED`.
- Delete: `204 NO_CONTENT`.
- Normal successful reads/updates default to `200 OK`.
