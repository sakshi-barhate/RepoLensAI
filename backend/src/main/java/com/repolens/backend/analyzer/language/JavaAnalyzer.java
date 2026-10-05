package com.repolens.backend.analyzer.language;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class JavaAnalyzer implements LanguageAnalyzer {

    @Override
    public String getLanguage() {
        return "Java";
    }

    @Override
    public LanguageMetrics analyze(File repositoryDirectory) {

        LanguageMetrics metrics = new LanguageMetrics();

        if (repositoryDirectory == null || !repositoryDirectory.exists()) {
            return metrics;
        }

        try (Stream<java.nio.file.Path> paths = Files.walk(repositoryDirectory.toPath())) {

            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {

                        try {
                            String content = Files.readString(path);

                            metrics.setLinesOfCode(
                                    metrics.getLinesOfCode()
                                            + countLines(content)
                            );

                            metrics.setClasses(
                                    metrics.getClasses()
                                            + countMatches(
                                                    content,
                                                    "\\bclass\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                                            )
                            );

                            metrics.setInterfaces(
                                    metrics.getInterfaces()
                                            + countMatches(
                                                    content,
                                                    "\\binterface\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                                            )
                            );

                            metrics.setPackages(
                                    metrics.getPackages()
                                            + countMatches(
                                                    content,
                                                    "\\bpackage\\s+[A-Za-z_$][A-Za-z0-9_$.]*\\s*;"
                                            )
                            );

                            metrics.setFunctions(
                                    metrics.getFunctions()
                                            + countJavaMethods(content)
                            );

                        } catch (Exception ignored) {
                        }
                    });

        } catch (Exception ignored) {
        }

        return metrics;
    }

    private int countLines(String content) {

        if (content == null || content.isBlank()) {
            return 0;
        }

        return (int) content.lines()
                .filter(line -> !line.trim().isEmpty())
                .count();
    }

    private int countMatches(String content, String regex) {

        if (content == null || content.isBlank()) {
            return 0;
        }

        return (int) Pattern.compile(regex)
                .matcher(content)
                .results()
                .count();
    }

    private int countJavaMethods(String content) {

        if (content == null || content.isBlank()) {
            return 0;
        }

        String methodRegex =
                "(public|private|protected|static|final|synchronized|native|abstract|default|\\s)+"
                        + "[A-Za-z_$][A-Za-z0-9_$<>\\[\\], ?]*\\s+"
                        + "[A-Za-z_$][A-Za-z0-9_$]*\\s*\\([^;{}]*\\)\\s*\\{";

        return countMatches(content, methodRegex);
    }
}