package com.repolens.backend.analyzer;

import com.repolens.backend.dto.RepositorySizeInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class RepositorySizeAnalyzer {

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
            "out"
    );

    public RepositorySizeInfo analyze(File repositoryDirectory) {

        long totalBytes = 0;
        long sourceBytes = 0;
        long resourceBytes = 0;
        long testBytes = 0;
        long totalFiles = 0;

        Map<String, Long> fileTypeBytes = new HashMap<>();

        try (Stream<Path> paths = Files.walk(repositoryDirectory.toPath())) {

            for (Path path : (Iterable<Path>) paths::iterator) {

                if (!Files.isRegularFile(path) || isExcluded(path, repositoryDirectory)) {
                    continue;
                }

                long size = Files.size(path);

                totalBytes += size;
                totalFiles++;

                String filePath = path.toString().replace("\\", "/");

                if (isSourceFile(filePath)) {
                    sourceBytes += size;
                }

                if (filePath.contains("/src/main/resources/")) {
                    resourceBytes += size;
                }

                if (filePath.contains("/src/test/")) {
                    testBytes += size;
                }

                String fileType = getFileType(path.getFileName().toString());

                fileTypeBytes.merge(fileType, size, Long::sum);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        double totalSizeMB = totalBytes / (1024.0 * 1024.0);
        double sourceSizeMB = sourceBytes / (1024.0 * 1024.0);
        double resourceSizeMB = resourceBytes / (1024.0 * 1024.0);
        double testSizeMB = testBytes / (1024.0 * 1024.0);

        double averageFileKB =
                totalFiles == 0 ? 0 : (totalBytes / 1024.0) / totalFiles;

        Map<String, Double> fileTypeSizes = new HashMap<>();

        fileTypeBytes.forEach((type, bytes) ->
                fileTypeSizes.put(type, round(bytes / (1024.0 * 1024.0)))
        );

        return RepositorySizeInfo.builder()
                .totalSizeMB(round(totalSizeMB))
                .sourceCodeMB(round(sourceSizeMB))
                .resourcesMB(round(resourceSizeMB))
                .testFilesMB(round(testSizeMB))
                .averageFileKB(round(averageFileKB))
                .fileTypeSizes(fileTypeSizes)
                .build();
    }

    private boolean isExcluded(Path path, File repositoryDirectory) {

        Path relativePath =
                repositoryDirectory.toPath().relativize(path);

        for (Path part : relativePath) {

            if (EXCLUDED_DIRECTORIES.contains(part.toString())) {
                return true;
            }
        }

        return false;
    }

    private boolean isSourceFile(String filePath) {

        return filePath.matches(
                ".*\\.(java|c|h|cpp|cc|cxx|hpp|py|js|jsx|ts|tsx|cs|go|rs|php|kt|kts)$"
        );
    }

    private String getFileType(String fileName) {

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "Other";
        }

        return fileName
                .substring(dotIndex + 1)
                .toUpperCase();
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}