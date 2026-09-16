package com.example.tasks.task.mapper;

import com.example.tasks.task.entity.TaskEntity;
import com.example.tasks.task.model.Task;

public final class TaskEntityMapper {

    private TaskEntityMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static TaskEntity toEntity(Task model) {
        if (model == null) {
            return null;
        }

        TaskEntity entity = TaskEntity.builder()
                .withId(model.getId())
                .withTitle(model.getTitle())
                .withDescription(model.getDescription())
                .withCompleted(model.isCompleted())
                .withCreatedAt(model.getCreatedAt())
                .build();

        return entity;
    }

    public static Task toModel(TaskEntity entity) {
        if (entity == null) {
            return null;
        }

        Task model = Task.builder()
                .withId(entity.getId())
                .withTitle(entity.getTitle())
                .withDescription(entity.getDescription())
                .withCompleted(entity.isCompleted())
                .withCreatedAt(entity.getCreatedAt())
                .build();

        return model;
    }
}
