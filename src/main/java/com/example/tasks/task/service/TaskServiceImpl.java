package com.example.tasks.task.service;

import com.example.tasks.common.exception.AppErrorMessage;
import com.example.tasks.common.exception.AppException;
import com.example.tasks.task.entity.TaskEntity;
import com.example.tasks.task.mapper.TaskEntityMapper;
import com.example.tasks.task.model.Task;
import com.example.tasks.task.model.TaskPatch;
import com.example.tasks.task.repository.TaskRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public Task create(Task task) {
        task.setCreatedAt(Instant.now());
        TaskEntity entity = TaskEntityMapper.toEntity(task);
        TaskEntity saved = taskRepository.save(entity);
        Task created = TaskEntityMapper.toModel(saved);
        log.info("[TASK] - ACTION: createTask: taskId: {}", created.getId());

        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Task getById(Long id) {
        TaskEntity entity = findById(id);
        Task task = TaskEntityMapper.toModel(entity);

        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> search(String title, Boolean completed, Pageable pageable) {
        boolean hasTitle = StringUtils.hasText(title);
        Page<TaskEntity> entities;
        if (hasTitle && completed != null) {
            entities = taskRepository.findByTitleContainingIgnoreCaseAndCompleted(title.trim(), completed, pageable);
        } else if (hasTitle) {
            entities = taskRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);
        } else if (completed != null) {
            entities = taskRepository.findByCompleted(completed, pageable);
        } else {
            entities = taskRepository.findAll(pageable);
        }

        Page<Task> tasks = entities.map(TaskEntityMapper::toModel);

        return tasks;
    }

    @Override
    public Task patch(Long id, TaskPatch patch) {
        TaskEntity entity = findById(id);
        applyPatch(entity, patch);
        TaskEntity saved = taskRepository.save(entity);
        Task updated = TaskEntityMapper.toModel(saved);
        log.info("[TASK] - ACTION: patchTask: taskId: {}", updated.getId());

        return updated;
    }

    @Override
    public void delete(Long id) {
        TaskEntity entity = findById(id);
        taskRepository.delete(entity);
        log.info("[TASK] - ACTION: deleteTask: taskId: {}", id);
    }

    private TaskEntity findById(Long id) {
        TaskEntity entity =
                taskRepository.findById(id).orElseThrow(() -> new AppException(AppErrorMessage.TASK_NOT_FOUND));

        return entity;
    }

    private void applyPatch(TaskEntity entity, TaskPatch patch) {
        if (patch.getTitle() != null) {
            entity.setTitle(patch.getTitle());
        }
        if (patch.getDescription() != null) {
            entity.setDescription(patch.getDescription());
        }
        if (patch.getCompleted() != null) {
            entity.setCompleted(patch.getCompleted());
        }
    }
}
