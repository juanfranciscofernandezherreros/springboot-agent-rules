package com.example.tasks.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.tasks.common.exception.AppErrorMessage;
import com.example.tasks.common.exception.AppException;
import com.example.tasks.common.exception.GlobalExceptionHandler;
import com.example.tasks.task.model.Task;
import com.example.tasks.task.model.TaskPatch;
import com.example.tasks.task.service.TaskService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    private static final long TASK_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void create_returns_created_task_ok() throws Exception {
        // given
        when(taskService.create(any(Task.class))).thenReturn(task());

        // when / then
        mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "title": "Write tests",
                                  "description": "Cover the API",
                                  "completed": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void create_with_blank_title_returns_validation_error_ko() throws Exception {
        // when / then
        mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title": "", "completed": false}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations[0].field").value("title"));
    }

    @Test
    void get_by_id_returns_task_ok() throws Exception {
        // given
        when(taskService.getById(TASK_ID)).thenReturn(task());

        // when / then
        mockMvc.perform(get("/tasks/{id}", TASK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID));
    }

    @Test
    void get_by_id_when_missing_returns_not_found_ko() throws Exception {
        // given
        when(taskService.getById(TASK_ID)).thenThrow(new AppException(AppErrorMessage.TASK_NOT_FOUND));

        // when / then
        mockMvc.perform(get("/tasks/{id}", TASK_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
    }

    @Test
    void search_returns_page_envelope_ok() throws Exception {
        // given
        var page = new PageImpl<>(List.of(task()));
        when(taskService.search(any(), any(), any(Pageable.class))).thenReturn(page);

        // when / then
        mockMvc.perform(get("/tasks/search").param("title", "tests").param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(TASK_ID))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void patch_returns_updated_task_ok() throws Exception {
        // given
        var updated = task();
        updated.setCompleted(true);
        when(taskService.patch(any(), any(TaskPatch.class))).thenReturn(updated);

        // when / then
        mockMvc.perform(
                        patch("/tasks/{id}", TASK_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"completed": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void delete_returns_no_content_ok() throws Exception {
        // when / then
        mockMvc.perform(delete("/tasks/{id}", TASK_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(taskService).delete(TASK_ID);
    }

    private Task task() {
        var task = Task.builder()
                .withId(TASK_ID)
                .withTitle("Write tests")
                .withDescription("Cover the API")
                .withCompleted(false)
                .withCreatedAt(Instant.parse("2026-01-01T10:00:00Z"))
                .build();

        return task;
    }
}
