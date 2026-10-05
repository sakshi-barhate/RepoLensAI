package com.repolens.backend.exception;

public class GitHubApiException extends RuntimeException {

    public GitHubApiException(String message) {
        super(message);
    }
}