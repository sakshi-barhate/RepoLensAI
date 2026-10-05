package com.repolens.backend.analyzer;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ProjectStructureAnalyzer {

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git",
            ".github",
            ".idea",
            ".vscode",
            ".mvn",
            "target",
            "build",
            "node_modules",
            ".gradle"
    );

    public List<String> detectProjectStructure(File repositoryDirectory) {

        List<String> folders = new ArrayList<>();

        scanDirectories(repositoryDirectory, repositoryDirectory, folders);

        return folders;
    }

    private void scanDirectories(File root,
                                 File current,
                                 List<String> folders) {

        File[] files = current.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (!file.isDirectory()) {
                continue;
            }

            if (IGNORED_DIRECTORIES.contains(file.getName())) {
                continue;
            }

            String relativePath = root.toPath()
                    .relativize(file.toPath())
                    .toString()
                    .replace("\\", "/");

            folders.add(relativePath);

            scanDirectories(root, file, folders);
        }
    }
}