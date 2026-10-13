package br.com.fiap.healthtrack.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> errors
) {
    public record FieldError(String campo, String mensagem) {}

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse ofValidation(int status, String error, String message, String path,
                                             List<FieldError> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }
}
