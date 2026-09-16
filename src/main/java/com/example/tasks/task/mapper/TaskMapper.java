package com.example.tasks.task.mapper;

import com.example.tasks.task.dto.CreateTaskRequest;
import com.example.tasks.task.dto.TaskPageResponse;
import com.example.tasks.task.dto.TaskResponse;
import com.example.tasks.task.dto.UpdateTaskRequest;
import com.example.tasks.task.model.Task;
import com.example.tasks.task.model.TaskPatch;
import java.util.List;
import org.springframework.data.domain.Page;

public final class TaskMapper {

    private TaskMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static Task toModel(CreateTaskRequest dto) {
        if (dto == null) {
            return null;
        }

        Task task = Task.builder()
                .withTitle(dto.title())
                .withDescription(dto.description())
                .withCompleted(dto.completed())
                .build();

        return task;
    }

    public static TaskPatch toModel(UpdateTaskRequest dto) {
        if (dto == null) {
            return null;
        }

        TaskPatch patch = TaskPatch.builder()
                .withTitle(dto.title())
                .withDescription(dto.description())
                .withCompleted(dto.completed())
                .build();

        return patch;
    }

    public static TaskResponse toDto(Task model) {
        if (model == null) {
            return null;
        }

        TaskResponse response = new TaskResponse(
                model.getId(), model.getTitle(), model.getDescription(), model.isCompleted(), model.getCreatedAt());

        return response;
    }

    public static TaskPageResponse toDto(Page<Task> page) {
        if (page == null) {
            return null;
        }

        List<TaskResponse> content =
                page.getContent().stream().map(TaskMapper::toDto).toList();
        TaskPageResponse response = new TaskPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());

        return response;
    }
}
