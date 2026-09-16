package com.example.tasks.cucumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.tasks.task.repository.TaskRepository;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

public class TaskStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private TaskRepository taskRepository;

    private MvcResult lastResponse;
    private Long taskId;

    @Before
    public void cleanDatabase() {
        taskRepository.deleteAll();
        taskId = null;
        lastResponse = null;
    }

    @Dado("que no hay tareas")
    public void noTasksExist() {}

    @Dado("que existe una tarea con título {string}")
    public void taskExists(String title) throws Exception {
        createTask(title, "Descripción de prueba");
        assertThat(lastResponse.getResponse().getStatus()).isEqualTo(201);
    }

    @Cuando("creo una tarea con título {string} y descripción {string}")
    public void createTask(String title, String description) throws Exception {
        String body =
                jsonMapper.writeValueAsString(Map.of("title", title, "description", description, "completed", false));
        lastResponse = mockMvc.perform(
                        post("/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        if (lastResponse.getResponse().getStatus() == 201) {
            Number id = JsonPath.read(lastResponse.getResponse().getContentAsString(), "$.id");
            taskId = id.longValue();
        }
    }

    @Cuando("consulto la tarea creada")
    public void getCreatedTask() throws Exception {
        lastResponse = mockMvc.perform(get("/tasks/{id}", taskId)).andReturn();
    }

    @Cuando("consulto la tarea con id {long}")
    public void getTaskById(long id) throws Exception {
        lastResponse = mockMvc.perform(get("/tasks/{id}", id)).andReturn();
    }

    @Cuando("busco tareas por el título {string} con tamaño de página {int}")
    public void searchTasks(String title, int pageSize) throws Exception {
        lastResponse = mockMvc.perform(get("/tasks/search")
                        .param("title", title)
                        .param("page", "0")
                        .param("size", String.valueOf(pageSize)))
                .andReturn();
    }

    @Cuando("marco la tarea creada como completada")
    public void completeCreatedTask() throws Exception {
        String body = jsonMapper.writeValueAsString(Map.of("completed", true));
        lastResponse = mockMvc.perform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
    }

    @Cuando("elimino la tarea creada")
    public void deleteCreatedTask() throws Exception {
        lastResponse = mockMvc.perform(delete("/tasks/{id}", taskId)).andReturn();
    }

    @Entonces("la respuesta tiene estado {int}")
    public void responseHasStatus(int status) {
        assertThat(lastResponse.getResponse().getStatus()).isEqualTo(status);
    }

    @Entonces("la respuesta contiene el título {string}")
    public void responseContainsTitle(String title) throws Exception {
        String actualTitle = JsonPath.read(lastResponse.getResponse().getContentAsString(), "$.title");
        assertThat(actualTitle).isEqualTo(title);
    }

    @Entonces("la respuesta indica que está completada")
    public void responseIsCompleted() throws Exception {
        Boolean completed = JsonPath.read(lastResponse.getResponse().getContentAsString(), "$.completed");
        assertThat(completed).isTrue();
    }

    @Entonces("la búsqueda devuelve {int} tarea")
    public void searchReturnsTasks(int expectedCount) throws Exception {
        List<?> content = JsonPath.read(lastResponse.getResponse().getContentAsString(), "$.content");
        assertThat(content).hasSize(expectedCount);
    }

    @Entonces("el código de error es {string}")
    public void errorCodeIs(String expectedCode) throws Exception {
        String code = JsonPath.read(lastResponse.getResponse().getContentAsString(), "$.code");
        assertThat(code).isEqualTo(expectedCode);
    }

    @Entonces("la respuesta contiene una infracción para el campo {string}")
    public void responseContainsFieldViolation(String field) throws Exception {
        List<String> fields = JsonPath.read(lastResponse.getResponse().getContentAsString(), "$.violations[*].field");
        assertThat(fields).contains(field);
    }
}
