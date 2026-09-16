package com.example.tasks.task.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Task {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private Instant createdAt;
}
