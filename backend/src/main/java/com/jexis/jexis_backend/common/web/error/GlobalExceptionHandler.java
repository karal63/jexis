package com.jexis.jexis_backend.common.web.error;

import com.jexis.jexis_backend.common.ratelimit.RateLimitException;

import java.util.*;
import java.util.stream.Collectors;

import com.jexis.jexis_backend.common.validation.ValidationError;
import com.jexis.jexis_backend.common.validation.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Catches all exceptions of type {@link DomainException} thrown by any
 * controller
 * <p>
 * When a {@link DomainException} is thrown, this handler constructs an to
 * custom error shape defined by {@link ErrorResponse} and returns it with a
 * specified HTTP status code. This allows for consistent error responses across
 * the application, providing clients with structured information about what
 * went wrong, including an error code, message, and HTTP status.
 * <p>
 * Author: Leo
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @org.springframework.beans.factory.annotation.Value("${application.errors.include-details:false}")
    private boolean includeDetails;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "NOT_FOUND", "Resource not found"));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handle(DomainException ex) {
        return ResponseEntity
                .status(ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST.value())
                .body(new ErrorResponse(ex.getStatus(), ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitException ex) {
        return ResponseEntity.status(ex.status())
                .header("Retry-After", Long.toString(ex.retryAfter()))
                .header("Cache-Control", "no-store")
                .body(new ErrorResponse(ex.status(), ex.code(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleServerError(Exception ex) {
        log.error("Unhandled request failure", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "UNKNOWN_ERROR", includeDetails ? ex.getMessage() : "Internal server error"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        List<ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR",
                        "Validation error",
                        errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleBodyNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife &&
                ife.getTargetType().isEnum()) {

            String field = ife.getPath().stream()
                    .findFirst()
                    .map(ref -> ref.getPropertyName())
                    .orElse("unknown");

            String message = "Invalid value '%s'. Allowed values: %s"
                    .formatted(
                            ife.getValue(),
                            Arrays.toString(ife.getTargetType().getEnumConstants())
                    );

            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "VALIDATION_ERROR",
                            "Validation error",
                            List.of(new ValidationError(field, message))
                    ));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(
                new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "REQ_BODY_MISSING",
                        includeDetails ? ex.getMessage() : "Invalid request body",
                        null
                ));
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
                .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "USER_NOT_AUTHORIZED", "Access denied"));
    }
}

