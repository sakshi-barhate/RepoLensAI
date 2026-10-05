package com.repolens.backend.analyzer.language;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class CAnalyzer implements LanguageAnalyzer {

    @Override
    public String getLanguage() {
        return "C";
    }

    @Override
    public LanguageMetrics analyze(File repositoryDirectory) {

        LanguageMetrics metrics = new LanguageMetrics();

        if (repositoryDirectory == null || !repositoryDirectory.exists()) {
            return metrics;
        }

        try (Stream<Path> paths = Files.walk(repositoryDirectory.toPath())) {

            paths.filter(Files::isRegularFile)
                    .filter(this::isCFile)
                    .forEach(path -> {

                        try {
                            String content = Files.readString(path);

                            metrics.setLinesOfCode(
                                    metrics.getLinesOfCode()
                                            + countLines(content)
                            );

                            metrics.setFunctions(
                                    metrics.getFunctions()
                                            + countFunctions(content)
                            );

                        } catch (Exception ignored) {
                        }
                    });

        } catch (Exception ignored) {
        }

        return metrics;
    }

    private boolean isCFile(Path path) {

        String name = path.getFileName()
                .toString()
                .toLowerCase();

        return name.endsWith(".c");
    }

    private int countLines(String content) {

        if (content == null || content.isBlank()) {
            return 0;
        }

        return (int) content.lines()
                .filter(line -> !line.trim().isEmpty())
                .count();
    }

    private int countFunctions(String content) {

        if (content == null || content.isBlank()) {
            return 0;
        }

        String[] lines = content.split("\\R");

        int count = 0;

        for (String line : lines) {

            String trimmed = line.trim();

            if (trimmed.contains("(")
                    && trimmed.contains(")")
                    && trimmed.endsWith("{")
                    && !trimmed.startsWith("if")
                    && !trimmed.startsWith("for")
                    && !trimmed.startsWith("while")
                    && !trimmed.startsWith("switch")) {

                count++;
            }
        }

        return count;
    }
}