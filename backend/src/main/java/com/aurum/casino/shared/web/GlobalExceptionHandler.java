package com.aurum.casino.shared.web;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aurum.casino.shared.error.DomainException;
import com.aurum.casino.shared.error.ErrorCode;

/**
 * Traduce excepciones a respuestas RFC 7807 (ProblemDetail) con un campo {@code code}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(DomainException ex) {
        return build(statusOf(ex.getCode()), ex.getCode().name(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex) {
        // Los records que se autovalidan lanzan DomainException durante la deserialización
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof DomainException domain) {
                return handleDomain(domain);
            }
        }
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "El cuerpo de la petición no es válido");
    }

    private static HttpStatus statusOf(ErrorCode code) {
        return switch (code) {
            case INVALID_BET, REFILL_NOT_AVAILABLE, INVALID_ACTION -> HttpStatus.BAD_REQUEST;
            case ACTIVE_SESSION -> HttpStatus.CONFLICT;
            case NO_ACTIVE_SESSION -> HttpStatus.NOT_FOUND;
            case INSUFFICIENT_FUNDS -> HttpStatus.UNPROCESSABLE_ENTITY;
            case INVALID_CREDENTIALS, UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case USERNAME_TAKEN -> HttpStatus.CONFLICT;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
        };
    }

    private static ResponseEntity<ProblemDetail> build(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code);
        return ResponseEntity.status(status).body(problem);
    }
}
