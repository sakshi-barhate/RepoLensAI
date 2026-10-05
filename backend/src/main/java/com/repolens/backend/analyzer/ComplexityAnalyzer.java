package com.repolens.backend.analyzer;

import com.repolens.backend.dto.ComplexityInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class ComplexityAnalyzer {

    /*
     * Programming source files used for code complexity analysis.
     *
     * HTML, CSS, SCSS and SQL are intentionally excluded because
     * they are not programming-language source code for this metric.
     */
    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            ".java",
            ".c",
            ".h",
            ".cpp",
            ".cc",
            ".cxx",
            ".hpp",
            ".js",
            ".jsx",
            ".ts",
            ".tsx",
            ".py",
            ".cs",
            ".go",
            ".rs",
            ".php",
            ".rb",
            ".swift",
            ".kt",
            ".kts",
            ".scala"
    );

    /*
     * Directories that should never be included in source-code analysis.
     */
    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(
            ".git",
            ".idea",
            ".vscode",
            "node_modules",
            "target",
            "build",
            "dist",
            "vendor",
            "bin",
            "obj",
            "out",
            ".next",
            ".gradle"
    );

    public ComplexityInfo analyze(File repositoryDirectory) {

        ComplexityStats stats = new ComplexityStats();

        scan(repositoryDirectory, stats);

        int averageLinesPerFile = stats.sourceFiles == 0
                ? 0
                : stats.totalLines / stats.sourceFiles;

        String complexity;

        if (stats.totalLines < 2000) {
            complexity = "Low";
        } else if (stats.totalLines < 10000) {
            complexity = "Medium";
        } else {
            complexity = "High";
        }

        return ComplexityInfo.builder()
                .sourceFiles(stats.sourceFiles)
                .linesOfCode(stats.totalLines)
                .averageLinesPerFile(averageLinesPerFile)
                .largestFile(stats.largestFile)
                .largestFileLines(stats.largestFileLines)
                .complexity(complexity)
                .build();
    }

    private void scan(File file, ComplexityStats stats) {

        if (file == null || !file.exists()) {
            return;
        }

        /*
         * Recursively scan directories.
         */
        if (file.isDirectory()) {

            if (EXCLUDED_DIRECTORIES.contains(
                    file.getName().toLowerCase())) {
                return;
            }

            File[] children = file.listFiles();

            if (children != null) {
                for (File child : children) {
                    scan(child, stats);
                }
            }

            return;
        }

        /*
         * Only analyze supported programming-language files.
         */
        String fileName = file.getName().toLowerCase();
        String extension = getExtension(fileName);

        if (!SOURCE_EXTENSIONS.contains(extension)) {
            return;
        }

        stats.sourceFiles++;

        try (Stream<String> lines = Files.lines(file.toPath())) {

            int lineCount = (int) lines.count();

            stats.totalLines += lineCount;

            /*
             * Track the largest programming source file.
             */
            if (lineCount > stats.largestFileLines) {
                stats.largestFileLines = lineCount;
                stats.largestFile = file.getName();
            }

        } catch (IOException ignored) {
            /*
             * Ignore files that cannot be read.
             */
        }
    }

    private String getExtension(String fileName) {

        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1) {
            return "";
        }

        return fileName.substring(lastDot);
    }

    private static class ComplexityStats {

        int sourceFiles;

        int totalLines;

        String largestFile = "";

        int largestFileLines;
    }
}