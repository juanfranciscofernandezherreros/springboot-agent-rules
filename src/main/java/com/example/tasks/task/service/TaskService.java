package com.example.tasks.task.service;

import com.example.tasks.task.model.Task;
import com.example.tasks.task.model.TaskPatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    Task create(Task task);

    Task getById(Long id);

    Page<Task> search(String title, Boolean completed, Pageable pageable);

    Task patch(Long id, TaskPatch patch);

    void delete(Long id);
}
