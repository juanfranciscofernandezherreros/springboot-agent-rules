package com.example.tasks.task.controller;

import com.example.tasks.task.dto.CreateTaskRequest;
import com.example.tasks.task.dto.TaskPageResponse;
import com.example.tasks.task.dto.TaskResponse;
import com.example.tasks.task.dto.UpdateTaskRequest;
import com.example.tasks.task.mapper.TaskMapper;
import com.example.tasks.task.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@RequestBody @Valid CreateTaskRequest request) {
        var task = TaskMapper.toModel(request);
        var created = taskService.create(task);
        var response = TaskMapper.toDto(created);

        return response;
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable @Positive Long id) {
        var task = taskService.getById(id);
        var response = TaskMapper.toDto(task);

        return response;
    }

    @GetMapping("/search")
    public TaskPageResponse searchTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean completed,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        var tasks = taskService.search(title, completed, pageable);
        var response = TaskMapper.toDto(tasks);

        return response;
    }

    @PatchMapping("/{id}")
    public TaskResponse patchTask(@PathVariable @Positive Long id, @RequestBody @Valid UpdateTaskRequest request) {
        var patch = TaskMapper.toModel(request);
        var updated = taskService.patch(id, patch);
        var response = TaskMapper.toDto(updated);

        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable @Positive Long id) {
        taskService.delete(id);
    }
}
