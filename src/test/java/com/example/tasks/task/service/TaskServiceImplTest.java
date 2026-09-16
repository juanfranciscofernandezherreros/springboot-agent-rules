package com.example.tasks.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tasks.common.exception.AppErrorMessage;
import com.example.tasks.common.exception.AppException;
import com.example.tasks.task.entity.TaskEntity;
import com.example.tasks.task.model.Task;
import com.example.tasks.task.model.TaskPatch;
import com.example.tasks.task.repository.TaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final long TASK_ID = 1L;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void create_saves_and_returns_task_ok() {
        // given
        var task = Task.builder()
                .withTitle("Write tests")
                .withDescription("Cover the service")
                .build();
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            var entity = invocation.getArgument(0, TaskEntity.class);
            entity.setId(TASK_ID);

            return entity;
        });

        // when
        var result = taskService.create(task);

        // then
        var captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(captor.capture());
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(TASK_ID),
                () -> assertThat(result.getTitle()).isEqualTo("Write tests"),
                () -> assertThat(result.getCreatedAt()).isNotNull(),
                () -> assertThat(captor.getValue().getCreatedAt()).isNotNull());
    }

    @Test
    void get_by_id_returns_task_ok() {
        // given
        var entity = taskEntity();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(entity));

        // when
        var result = taskService.getById(TASK_ID);

        // then
        assertAll(() -> assertThat(result.getId()).isEqualTo(TASK_ID), () -> assertThat(result.getTitle())
                .isEqualTo("Write tests"));
    }

    @Test
    void get_by_id_when_missing_throws_ko() {
        // given
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> taskService.getById(TASK_ID))
                .isInstanceOf(AppException.class)
                .extracting("error")
                .isEqualTo(AppErrorMessage.TASK_NOT_FOUND);
    }

    @Test
    void search_with_title_and_completed_returns_page_ok() {
        // given
        var pageable = PageRequest.of(0, 10);
        var entityPage = new PageImpl<>(List.of(taskEntity()), pageable, 1);
        when(taskRepository.findByTitleContainingIgnoreCaseAndCompleted("tests", true, pageable))
                .thenReturn(entityPage);

        // when
        var result = taskService.search(" tests ", true, pageable);

        // then
        assertAll(
                () -> assertThat(result.getTotalElements()).isOne(),
                () -> assertThat(result.getContent()).extracting(Task::getId).containsExactly(TASK_ID));
    }

    @Test
    void search_without_filters_returns_page_ok() {
        // given
        var pageable = PageRequest.of(0, 10);
        var entityPage = new PageImpl<>(List.of(taskEntity()), pageable, 1);
        when(taskRepository.findAll(pageable)).thenReturn(entityPage);

        // when
        var result = taskService.search(null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(pageable);
    }

    @Test
    void patch_updates_only_supplied_fields_ok() {
        // given
        var entity = taskEntity();
        var patch = TaskPatch.builder().withCompleted(true).build();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(entity));
        when(taskRepository.save(entity)).thenReturn(entity);

        // when
        var result = taskService.patch(TASK_ID, patch);

        // then
        assertAll(
                () -> assertThat(result.getTitle()).isEqualTo("Write tests"),
                () -> assertThat(result.getDescription()).isEqualTo("Cover the service"),
                () -> assertThat(result.isCompleted()).isTrue());
    }

    @Test
    void patch_when_missing_does_not_save_ko() {
        // given
        var patch = TaskPatch.builder().withTitle("Updated").build();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> taskService.patch(TASK_ID, patch)).isInstanceOf(AppException.class);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void delete_existing_task_ok() {
        // given
        var entity = taskEntity();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(entity));

        // when
        taskService.delete(TASK_ID);

        // then
        verify(taskRepository).delete(entity);
    }

    @Test
    void delete_when_missing_does_not_delete_ko() {
        // given
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> taskService.delete(TASK_ID)).isInstanceOf(AppException.class);
        verify(taskRepository, never()).delete(any());
    }

    private TaskEntity taskEntity() {
        var entity = TaskEntity.builder()
                .withId(TASK_ID)
                .withTitle("Write tests")
                .withDescription("Cover the service")
                .withCompleted(false)
                .withCreatedAt(Instant.parse("2026-01-01T10:00:00Z"))
                .build();

        return entity;
    }
}
