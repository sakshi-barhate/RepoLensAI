package com.repolens.backend.analyzer;

import com.repolens.backend.dto.HealthScore;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class HealthScoreAnalyzer {

    public HealthScore analyze(File repository) {

        boolean readme =
                exists(repository, "README.md")
                        || exists(repository, "README")
                        || existsIgnoreCase(repository, "README.md")
                        || existsIgnoreCase(repository, "README");

        boolean license =
                exists(repository, "LICENSE")
                        || exists(repository, "LICENSE.md")
                        || exists(repository, "LICENSE.txt")
                        || existsIgnoreCase(repository, "LICENSE")
                        || existsIgnoreCase(repository, "LICENSE.md")
                        || existsIgnoreCase(repository, "LICENSE.txt");

        boolean gitignore =
                exists(repository, ".gitignore");

        boolean docker =
                exists(repository, "Dockerfile")
                        || existsIgnoreCase(repository, "Dockerfile");

        boolean dockerCompose =
                exists(repository, "docker-compose.yml")
                        || exists(repository, "docker-compose.yaml")
                        || exists(repository, "compose.yml")
                        || exists(repository, "compose.yaml");

        boolean githubActions =
                new File(repository, ".github/workflows").exists();

        boolean tests =
                new File(repository, "src/test").exists();

        boolean buildFile =
                exists(repository, "pom.xml")
                        || exists(repository, "build.gradle")
                        || exists(repository, "build.gradle.kts");

        int score = 0;

        if (readme) {
            score += 15;
        }

        if (license) {
            score += 10;
        }

        if (gitignore) {
            score += 10;
        }

        if (docker) {
            score += 10;
        }

        if (dockerCompose) {
            score += 10;
        }

        if (githubActions) {
            score += 15;
        }

        if (tests) {
            score += 15;
        }

        if (buildFile) {
            score += 15;
        }

        String grade;

        if (score >= 90) {
            grade = "A";
        } else if (score >= 75) {
            grade = "B";
        } else if (score >= 60) {
            grade = "C";
        } else if (score >= 40) {
            grade = "D";
        } else {
            grade = "F";
        }

        return HealthScore.builder()
                .score(score)
                .grade(grade)
                .readme(readme)
                .license(license)
                .gitignore(gitignore)
                .docker(docker)
                .dockerCompose(dockerCompose)
                .githubActions(githubActions)
                .tests(tests)
                .buildFile(buildFile)
                .build();
    }

    private boolean exists(File repository, String name) {
        return new File(repository, name).exists();
    }

    private boolean existsIgnoreCase(File repository, String name) {

        File[] files = repository.listFiles();

        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }
}