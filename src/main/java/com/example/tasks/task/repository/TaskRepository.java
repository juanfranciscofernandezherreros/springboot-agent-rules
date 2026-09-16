package com.example.tasks.task.repository;

import com.example.tasks.task.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Page<TaskEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<TaskEntity> findByCompleted(boolean completed, Pageable pageable);

    Page<TaskEntity> findByTitleContainingIgnoreCaseAndCompleted(String title, boolean completed, Pageable pageable);
}
