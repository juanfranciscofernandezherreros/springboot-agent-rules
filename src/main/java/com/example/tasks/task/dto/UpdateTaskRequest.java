package com.example.tasks.task.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") @Size(max = 200) String title,
        @Size(max = 2000) String description,
        Boolean completed) {}
