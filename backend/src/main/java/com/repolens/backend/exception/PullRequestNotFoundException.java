package com.repolens.backend.exception;

public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException(String message) {
        super(message);
    }
}