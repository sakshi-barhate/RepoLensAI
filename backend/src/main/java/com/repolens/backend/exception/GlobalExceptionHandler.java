package com.repolens.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PullRequestNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePullRequestNotFound(
            PullRequestNotFoundException exception) {

        Map<String, Object> response = Map.of(
                "message", exception.getMessage(),
                "status", HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(RepositoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRepositoryNotFound(
            RepositoryNotFoundException exception) {

        Map<String, Object> response = Map.of(
                "message", exception.getMessage(),
                "status", HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<Map<String, Object>> handleGitHubApiException(
            GitHubApiException exception) {

        Map<String, Object> response = Map.of(
                "message", exception.getMessage(),
                "status", HttpStatus.BAD_GATEWAY.value()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(response);
    }
}