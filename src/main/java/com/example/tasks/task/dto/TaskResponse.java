package com.example.tasks.task.dto;

import java.time.Instant;

public record TaskResponse(Long id, String title, String description, boolean completed, Instant createdAt) {}
