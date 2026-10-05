package com.repolens.backend.analyzer;

import com.repolens.backend.dto.DocumentationInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class DocumentationAnalyzer {

    public DocumentationInfo analyze(File repositoryDirectory) {

        int readmeFiles = 0;
        int markdownFiles = 0;
        int javaDocComments = 0;

        try (Stream<Path> paths = Files.walk(repositoryDirectory.toPath())) {

            for (Path path : (Iterable<Path>) paths::iterator) {

                String fileName = path.getFileName().toString().toLowerCase();

                if (fileName.equals("readme.md") || fileName.equals("readme")) {
                    readmeFiles++;
                }

                if (fileName.endsWith(".md")) {
                    markdownFiles++;
                }

                if (fileName.endsWith(".java")) {

                    for (String line : Files.readAllLines(path)) {

                        String trimmed = line.trim();

                        if (trimmed.startsWith("/**")) {
                            javaDocComments++;
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        String documentationScore;

        int score = readmeFiles + markdownFiles + javaDocComments;

        if (score >= 20) {
            documentationScore = "Excellent";
        } else if (score >= 8) {
            documentationScore = "Good";
        } else {
            documentationScore = "Poor";
        }

        return DocumentationInfo.builder()
                .javaDocComments(javaDocComments)
                .markdownFiles(markdownFiles)
                .readmeExists(readmeFiles > 0)
                .documentationScore(documentationScore)
                .build();
    }
}