package com.cosmosdb.demo.cosmosreactive.common;

import com.cosmosdb.demo.cosmosreactive.user.UserAlreadyExistsException;
import com.cosmosdb.demo.cosmosreactive.user.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException exception) {
        LOGGER.warn("GlobalExceptionHandler#handleUserNotFound - {}", exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), List.of(exception.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        LOGGER.warn("GlobalExceptionHandler#handleUserAlreadyExists - {}", exception.getMessage());
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), List.of(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        LOGGER.warn("GlobalExceptionHandler#handleMethodArgumentNotValid - {}", details);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        LOGGER.warn("GlobalExceptionHandler#handleConstraintViolation - {}", details);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception exception) {
        String origin = resolveOrigin(exception);
        LOGGER.error(
                "GlobalExceptionHandler#handleGenericException - Unhandled error at {} - {}",
                origin,
                exception.getMessage(),
                exception
        );
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", List.of(exception.getMessage()));
    }

    private String resolveOrigin(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                .filter(element -> element.getClassName().startsWith("com.cosmosdb.demo.cosmosreactive"))
                .findFirst()
                .map(element -> element.getClassName() + "#" + element.getMethodName() + ":" + element.getLineNumber())
                .orElse("unknown");
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String error, List<String> details) {
        ApiError apiError = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                error,
                status.getReasonPhrase(),
                details
        );
        return ResponseEntity.status(status).body(apiError);
    }
}
