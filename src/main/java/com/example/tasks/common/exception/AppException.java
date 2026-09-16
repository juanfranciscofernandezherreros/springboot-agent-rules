package com.example.tasks.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final AppErrorMessage error;

    public AppException(AppErrorMessage error) {
        super(error.getMessage());
        this.error = error;
    }
}
