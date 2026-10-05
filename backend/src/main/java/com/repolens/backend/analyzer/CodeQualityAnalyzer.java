package com.repolens.backend.analyzer;

import com.repolens.backend.dto.CodeQualityInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class CodeQualityAnalyzer {

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
            ".scala",
            ".html",
            ".css",
            ".scss",
            ".sql"
    );

    public CodeQualityInfo analyze(File repositoryDirectory) {

        int codeLines = 0;
        int blankLines = 0;
        int commentLines = 0;
        int todoComments = 0;
        int fixmeComments = 0;

        int systemOutCount = 0;
        int printStackTraceCount = 0;
        int emptyCatchCount = 0;

        try (Stream<Path> paths = Files.walk(repositoryDirectory.toPath())) {

            for (Path path : (Iterable<Path>) paths::iterator) {

                if (!isSourceFile(path)) {
                    continue;
                }

                String extension = getExtension(path);
                boolean blockComment = false;

                try {

                    for (String line : Files.readAllLines(path)) {

                        String trimmed = line.trim();

                        if (trimmed.isEmpty()) {
                            blankLines++;
                            continue;
                        }

                        String upperLine = trimmed.toUpperCase();

                        if (upperLine.contains("TODO")) {
                            todoComments++;
                        }

                        if (upperLine.contains("FIXME")) {
                            fixmeComments++;
                        }

                        if (trimmed.contains("System.out.println(")
                                && !path.toString().contains(
                                File.separator + "test" + File.separator)) {
                            systemOutCount++;
                        }

                        if (trimmed.contains("printStackTrace(")) {
                            printStackTraceCount++;
                        }

                        if (trimmed.matches(
                                "catch\\s*\\(.*\\)\\s*\\{\\s*\\}")) {
                            emptyCatchCount++;
                        }

                        // Continue an existing block comment.
                        if (blockComment) {

                            commentLines++;

                            if (trimmed.contains("*/")) {
                                blockComment = false;
                            }

                            continue;
                        }

                        // Start a block comment.
                        if (trimmed.startsWith("/*")) {

                            commentLines++;

                            if (!trimmed.contains("*/")) {
                                blockComment = true;
                            }

                            continue;
                        }

                        // Language-specific single-line comments.
                        if (isSingleLineComment(trimmed, extension)) {
                            commentLines++;
                            continue;
                        }

                        codeLines++;
                    }

                } catch (IOException ignored) {
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * Code smells are based only on detected maintainability issues.
         *
         * TODO                = 1
         * FIXME               = 2
         * System.out.println  = 2
         * printStackTrace     = 2
         * Empty catch block   = 3
         */
        int codeSmells =
                (todoComments * 1)
                        + (fixmeComments * 2)
                        + (systemOutCount * 2)
                        + (printStackTraceCount * 2)
                        + (emptyCatchCount * 3);

        System.out.println("========== CODE QUALITY ==========");
        System.out.println("TODO Comments        : " + todoComments);
        System.out.println("FIXME Comments       : " + fixmeComments);
        System.out.println("System.out.println() : " + systemOutCount);
        System.out.println("printStackTrace()    : " + printStackTraceCount);
        System.out.println("Empty Catch Blocks   : " + emptyCatchCount);
        System.out.println("Code Lines           : " + codeLines);
        System.out.println("Comment Lines        : " + commentLines);
        System.out.println("Blank Lines          : " + blankLines);
        System.out.println("Total Code Smells    : " + codeSmells);
        System.out.println("==================================");

        String maintainability;
        String technicalDebt;
        String rating;

        if (codeSmells <= 2) {
            maintainability = "Excellent";
            technicalDebt = "Low";
            rating = "A";
        } else if (codeSmells <= 8) {
            maintainability = "Good";
            technicalDebt = "Medium";
            rating = "B";
        } else {
            maintainability = "Poor";
            technicalDebt = "High";
            rating = "C";
        }

        return CodeQualityInfo.builder()
                .todoComments(todoComments)
                .fixmeComments(fixmeComments)
                .commentLines(commentLines)
                .blankLines(blankLines)
                .codeLines(codeLines)
                .codeSmells(codeSmells)
                .technicalDebt(technicalDebt)
                .maintainability(maintainability)
                .rating(rating)
                .build();
    }

    private boolean isSourceFile(Path path) {

        if (!Files.isRegularFile(path)) {
            return false;
        }

        String fileName = path.getFileName()
                .toString()
                .toLowerCase();

        for (String extension : SOURCE_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }

    private String getExtension(Path path) {

        String fileName = path.getFileName()
                .toString()
                .toLowerCase();

        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1) {
            return "";
        }

        return fileName.substring(lastDot);
    }

    private boolean isSingleLineComment(
            String line,
            String extension) {

        // Python
        if (extension.equals(".py")) {
            return line.startsWith("#");
        }

        // SQL
        if (extension.equals(".sql")) {
            return line.startsWith("--")
                    || line.startsWith("#");
        }

        // HTML
        if (extension.equals(".html")) {
            return line.startsWith("<!--");
        }

        // Ruby
        if (extension.equals(".rb")) {
            return line.startsWith("#");
        }

        // Java, JavaScript, TypeScript, C, C++,
        // C#, Go, Rust, PHP, Swift, Kotlin, Scala, etc.
        if (line.startsWith("//")) {
            return true;
        }

        return false;
    }
}