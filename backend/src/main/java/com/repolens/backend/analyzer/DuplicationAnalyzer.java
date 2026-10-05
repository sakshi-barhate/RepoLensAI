package com.repolens.backend.analyzer;

import com.repolens.backend.dto.DuplicationInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DuplicationAnalyzer {

    public DuplicationInfo analyze(File repositoryDirectory) {

        Map<String, Integer> classCounts = new HashMap<>();
        List<String> duplicatedClasses = new ArrayList<>();

        scan(repositoryDirectory, classCounts);

        int duplicateFiles = 0;

        for (Map.Entry<String, Integer> entry : classCounts.entrySet()) {
            if (entry.getValue() > 1) {
                duplicateFiles += entry.getValue();
                duplicatedClasses.add(entry.getKey());
            }
        }

        String risk;

        if (duplicateFiles == 0) {
            risk = "Low";
        } else if (duplicateFiles <= 5) {
            risk = "Medium";
        } else {
            risk = "High";
        }

        return DuplicationInfo.builder()
                .duplicateFiles(duplicateFiles)
                .duplicatedClasses(duplicatedClasses)
                .risk(risk)
                .build();
    }

    private void scan(File file, Map<String, Integer> classCounts) {

        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    scan(child, classCounts);
                }
            }
            return;
        }

        if (!file.getName().endsWith(".java")) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath());

            for (String line : lines) {
                line = line.trim();

                if (line.startsWith("public class ")
                        || line.startsWith("class ")
                        || line.startsWith("public final class ")
                        || line.startsWith("final class ")) {

                    String[] parts = line.split("\\s+");

                    for (int i = 0; i < parts.length; i++) {
                        if ("class".equals(parts[i]) && i + 1 < parts.length) {

                            String className = parts[i + 1]
                                    .replace("{", "")
                                    .trim();

                            classCounts.put(
                                    className,
                                    classCounts.getOrDefault(className, 0) + 1
                            );
                            break;
                        }
                    }
                }
            }

        } catch (Exception ignored) {
        }
    }
}