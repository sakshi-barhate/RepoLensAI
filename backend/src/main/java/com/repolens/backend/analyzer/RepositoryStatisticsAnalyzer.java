package com.repolens.backend.analyzer;

import com.repolens.backend.dto.RepositoryStatistics;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class RepositoryStatisticsAnalyzer {

    public RepositoryStatistics analyze(File repositoryDirectory) {

        int totalFiles = countFiles(repositoryDirectory);
        int totalDirectories = countDirectories(repositoryDirectory);

        return RepositoryStatistics.builder()
                .totalFiles(totalFiles)
                .totalDirectories(totalDirectories)
                .build();
    }

    private int countFiles(File directory) {

        File[] files = directory.listFiles();

        if (files == null) {
            return 0;
        }

        int count = 0;

        for (File file : files) {

            if (file.isFile()) {
                count++;
            } else {
                count += countFiles(file);
            }
        }

        return count;
    }

    private int countDirectories(File directory) {

        File[] files = directory.listFiles();

        if (files == null) {
            return 0;
        }

        int count = 0;

        for (File file : files) {

            if (file.isDirectory()) {
                count++;
                count += countDirectories(file);
            }
        }

        return count;
    }
}