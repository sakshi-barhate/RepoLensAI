package com.repolens.backend.analyzer;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class BuildToolDetector {

    public String detectBuildTool(File repositoryDirectory) {

        if (repositoryDirectory == null || !repositoryDirectory.exists()) {
            return "Unknown";
        }

        // Java
        if (containsFile(repositoryDirectory, "pom.xml")) {
            return "Maven";
        }

        if (containsFile(repositoryDirectory, "build.gradle")
                || containsFile(repositoryDirectory, "build.gradle.kts")) {
            return "Gradle";
        }

        // JavaScript / Node.js
        if (containsFile(repositoryDirectory, "package.json")) {
            return "NPM";
        }

        if (containsFile(repositoryDirectory, "pnpm-lock.yaml")) {
            return "PNPM";
        }

        if (containsFile(repositoryDirectory, "yarn.lock")) {
            return "Yarn";
        }

        // C / C++
        if (containsFile(repositoryDirectory, "CMakeLists.txt")) {
            return "CMake";
        }

        if (containsFile(repositoryDirectory, "Makefile")) {
            return "Make";
        }

        // Python
        if (containsFile(repositoryDirectory, "pyproject.toml")) {
            return "PyProject";
        }

        if (containsFile(repositoryDirectory, "requirements.txt")) {
            return "Pip";
        }

        if (containsFile(repositoryDirectory, "Pipfile")) {
            return "Pipenv";
        }

        if (containsFile(repositoryDirectory, "poetry.lock")) {
            return "Poetry";
        }

        // Go
        if (containsFile(repositoryDirectory, "go.mod")) {
            return "Go Modules";
        }

        // Rust
        if (containsFile(repositoryDirectory, "Cargo.toml")) {
            return "Cargo";
        }

        // PHP
        if (containsFile(repositoryDirectory, "composer.json")) {
            return "Composer";
        }

        // Ruby
        if (containsFile(repositoryDirectory, "Gemfile")) {
            return "Bundler";
        }

        return "Unknown";
    }

    private boolean containsFile(File directory, String fileName) {

        File[] files = directory.listFiles();

        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.getName().equalsIgnoreCase(fileName)) {
                return true;
            }
        }

        return false;
    }
}