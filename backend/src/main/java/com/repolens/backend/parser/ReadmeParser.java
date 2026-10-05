package com.repolens.backend.parser;

import com.repolens.backend.dto.ReadmeInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReadmeParser {

    public ReadmeInfo extractReadme(File repositoryDirectory) {

        File readme = new File(repositoryDirectory, "README.md");

        if (!readme.exists()) {
            return ReadmeInfo.builder()
                    .title("README not found")
                    .description("")
                    .technologies(new ArrayList<>())
                    .build();
        }

        try {

            String content = Files.readString(readme.toPath());

            return ReadmeInfo.builder()
                    .title(extractTitle(content))
                    .description(extractDescription(content))
                    .technologies(detectTechnologies(content))
                    .build();

        } catch (Exception e) {

            return ReadmeInfo.builder()
                    .title("Unable to read README")
                    .description("")
                    .technologies(new ArrayList<>())
                    .build();
        }
    }

    private String extractTitle(String content) {

        for (String line : content.split("\n")) {

            line = line.trim();

            if (line.startsWith("# ")) {

                String title = line.substring(2).trim();

                title = title.replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", "");
                title = title.replaceAll("\\[[^\\]]*\\]\\([^)]*\\)", "");
                title = title.replaceAll("\\s+", " ").trim();

                return title;
            }
        }

        return "Unknown";
    }

    private String extractDescription(String content) {

        String[] lines = content.split("\n");

        for (String line : lines) {

            line = line.trim();

            if (line.isBlank())
                continue;

            if (line.startsWith("#"))
                continue;

            // Skip badge/image lines
            if (line.contains("!["))
                continue;

            // Skip markdown-only links
            if (line.startsWith("["))
                continue;

            // Skip HTML image tags
            if (line.contains("<img"))
                continue;

            // Skip blockquotes
            if (line.startsWith(">"))
                continue;

            // Skip URLs
            if (line.startsWith("http"))
                continue;

            // Skip separator lines
            if (line.matches("^-{3,}$"))
                continue;

            line = line.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "$1");
            line = line.replaceAll("`", "");
            line = line.replaceAll("\\*", "");
            line = line.trim();

            if (!line.isBlank()) {
                return line;
            }
        }

        return "";
    }

    private List<String> detectTechnologies(String content) {

        List<String> technologies = new ArrayList<>();

        addIfPresent(content, "Spring Boot", technologies);
        addIfPresent(content, "Java", technologies);
        addIfPresent(content, "Maven", technologies);
        addIfPresent(content, "Gradle", technologies);
        addIfPresent(content, "React", technologies);
        addIfPresent(content, "Next.js", technologies);
        addIfPresent(content, "PostgreSQL", technologies);

        return technologies;
    }

    private void addIfPresent(String content, String keyword, List<String> technologies) {

        if (content.toLowerCase().contains(keyword.toLowerCase())) {
            technologies.add(keyword);
        }
    }
}