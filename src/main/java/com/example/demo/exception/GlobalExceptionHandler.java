package com.example.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NewsNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NewsNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ApiError("NOT_FOUND", "Новость не найдена"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(400)
                .body(new ApiError("VALIDATION_ERROR", "Некорректные данные запроса"));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiError> handleAuth(AuthException ex) {
        return ResponseEntity.status(401)
                .body(new ApiError("AUTH_ERROR", "Ошибка авторизации"));
    }

    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(org.springframework.security.authorization.AuthorizationDeniedException ex) {
        return ResponseEntity.status(403)
                .body(new ApiError("FORBIDDEN", "Доступ запрещён"));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiError> handleRateLimit(RateLimitException ex) {
        return ResponseEntity.status(429)
                .body(new ApiError("RATE_LIMIT", "Слишком много попыток входа. Попробуйте позже."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Внутренняя ошибка сервера", ex);
        return ResponseEntity.status(500)
                .body(new ApiError("INTERNAL_ERROR", "Внутренняя ошибка сервера"));
    }
}