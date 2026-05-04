package ru.alexeev.mygraduation.common.error;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class AppException extends RuntimeException {
    @Getter
    private final ErrorType errorType;

    public AppException(@NotNull String msg, ErrorType errorType) {
        super(msg);
        this.errorType = errorType;
    }
}
