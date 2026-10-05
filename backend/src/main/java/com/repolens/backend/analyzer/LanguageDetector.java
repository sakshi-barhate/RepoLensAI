package com.repolens.backend.analyzer;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
public class LanguageDetector {

    public String detectLanguage(File repositoryDirectory) {

        if (repositoryDirectory == null || !repositoryDirectory.exists()) {
            return "Unknown";
        }

        Map<String, Integer> languageCount = new HashMap<>();

        scanFiles(repositoryDirectory, languageCount);

        if (languageCount.isEmpty()) {
            return "Unknown";
        }

        return languageCount.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private void scanFiles(File directory, Map<String, Integer> languageCount) {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                // Ignore Git and build folders
                if (file.getName().equalsIgnoreCase(".git")
                        || file.getName().equalsIgnoreCase("node_modules")
                        || file.getName().equalsIgnoreCase("target")
                        || file.getName().equalsIgnoreCase("build")
                        || file.getName().equalsIgnoreCase("dist")) {
                    continue;
                }

                scanFiles(file, languageCount);

            } else {

                String language = getLanguageFromExtension(file.getName());

                if (language != null) {
                    languageCount.merge(language, 1, Integer::sum);
                }
            }
        }
    }

    private String getLanguageFromExtension(String fileName) {

        String name = fileName.toLowerCase();

        if (name.endsWith(".java")) {
            return "Java";
        }

        if (name.endsWith(".js")
                || name.endsWith(".jsx")
                || name.endsWith(".mjs")
                || name.endsWith(".cjs")) {
            return "JavaScript";
        }

        if (name.endsWith(".ts")
                || name.endsWith(".tsx")) {
            return "TypeScript";
        }

        if (name.endsWith(".py")) {
            return "Python";
        }

        if (name.endsWith(".c")) {
            return "C";
        }

        if (name.endsWith(".cpp")
                || name.endsWith(".cc")
                || name.endsWith(".cxx")
                || name.endsWith(".hpp")
                || name.endsWith(".hh")
                || name.endsWith(".hxx")) {
            return "C++";
        }

        if (name.endsWith(".cs")) {
            return "C#";
        }

        if (name.endsWith(".go")) {
            return "Go";
        }

        if (name.endsWith(".rs")) {
            return "Rust";
        }

        if (name.endsWith(".php")) {
            return "PHP";
        }

        if (name.endsWith(".kt")
                || name.endsWith(".kts")) {
            return "Kotlin";
        }

        if (name.endsWith(".swift")) {
            return "Swift";
        }

        if (name.endsWith(".rb")) {
            return "Ruby";
        }

        return null;
    }
}