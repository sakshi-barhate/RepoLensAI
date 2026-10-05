package com.repolens.backend.analyzer;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class FrameworkDetector {

    public String detectFramework(File repositoryDirectory) {

        if (repositoryDirectory == null || !repositoryDirectory.exists()) {
            return "Unknown";
        }

        if (containsSpringBootApplication(repositoryDirectory)) {
            return "Spring Boot";
        }

        if (containsFile(repositoryDirectory, "pom.xml")) {

            Path pom = repositoryDirectory.toPath().resolve("pom.xml");

            if (fileContains(pom, "quarkus")) {
                return "Quarkus";
            }

            if (fileContains(pom, "micronaut")) {
                return "Micronaut";
            }

            if (fileContains(pom, "struts")) {
                return "Apache Struts";
            }
        }

        if (containsFile(repositoryDirectory, "build.gradle")
                || containsFile(repositoryDirectory, "build.gradle.kts")) {

            Path gradleFile = repositoryDirectory.toPath()
                    .resolve(
                            containsFile(repositoryDirectory, "build.gradle")
                                    ? "build.gradle"
                                    : "build.gradle.kts"
                    );

            if (fileContains(gradleFile, "quarkus")) {
                return "Quarkus";
            }

            if (fileContains(gradleFile, "micronaut")) {
                return "Micronaut";
            }
        }

        if (containsFile(repositoryDirectory, "next.config.js")
                || containsFile(repositoryDirectory, "next.config.ts")
                || containsFile(repositoryDirectory, "next.config.mjs")) {
            return "Next.js";
        }

        if (containsFile(repositoryDirectory, "angular.json")) {
            return "Angular";
        }

        if (containsFile(repositoryDirectory, "nuxt.config.js")
                || containsFile(repositoryDirectory, "nuxt.config.ts")
                || containsFile(repositoryDirectory, "nuxt.config.mjs")) {
            return "Nuxt";
        }

        if (containsFile(repositoryDirectory, "svelte.config.js")) {
            return "Svelte";
        }

        if (containsFile(repositoryDirectory, "package.json")) {

            Path packageJson =
                    repositoryDirectory.toPath().resolve("package.json");

            if (fileContains(packageJson, "\"@nestjs/core\"")) {
                return "NestJS";
            }

            if (fileContains(packageJson, "\"express\"")) {
                return "Express";
            }

            if (fileContains(packageJson, "\"react\"")) {
                return "React";
            }

            if (fileContains(packageJson, "\"vue\"")) {
                return "Vue";
            }

            if (fileContains(packageJson, "\"koa\"")) {
                return "Koa";
            }

            if (fileContains(packageJson, "\"fastify\"")) {
                return "Fastify";
            }

            if (fileContains(packageJson, "\"hapi\"")) {
                return "Hapi";
            }
        }

        if (containsFile(repositoryDirectory, "manage.py")) {
            return "Django";
        }

        if (containsPythonDependency(repositoryDirectory, "flask")) {
            return "Flask";
        }

        if (containsPythonDependency(repositoryDirectory, "fastapi")) {
            return "FastAPI";
        }

        if (containsPythonDependency(repositoryDirectory, "django")) {
            return "Django";
        }

        if (containsPythonDependency(repositoryDirectory, "tornado")) {
            return "Tornado";
        }

        if (containsPythonDependency(repositoryDirectory, "pyramid")) {
            return "Pyramid";
        }

        if (containsRubyDependency(repositoryDirectory, "rails")) {
            return "Ruby on Rails";
        }

        if (containsRubyDependency(repositoryDirectory, "sinatra")) {
            return "Sinatra";
        }

        if (containsFile(repositoryDirectory, "config.ru")) {
            return "Rack";
        }

        if (containsPhpDependency(repositoryDirectory, "laravel")) {
            return "Laravel";
        }

        if (containsPhpDependency(repositoryDirectory, "symfony")) {
            return "Symfony";
        }

        if (containsPhpDependency(repositoryDirectory, "codeigniter")) {
            return "CodeIgniter";
        }

        if (containsFile(repositoryDirectory, "CMakeLists.txt")) {

            Path cmake =
                    repositoryDirectory.toPath().resolve("CMakeLists.txt");

            if (fileContains(cmake, "Qt")) {
                return "Qt";
            }

            if (fileContains(cmake, "wxWidgets")) {
                return "wxWidgets";
            }

            if (fileContains(cmake, "Boost")) {
                return "Boost";
            }
        }

        if (containsGoDependency(repositoryDirectory, "gin-gonic")) {
            return "Gin";
        }

        if (containsGoDependency(repositoryDirectory, "labstack/echo")) {
            return "Echo";
        }

        if (containsGoDependency(repositoryDirectory, "gofiber")) {
            return "Fiber";
        }

        if (containsGoDependency(repositoryDirectory, "go-chi")) {
            return "Chi";
        }

        if (containsRustDependency(repositoryDirectory, "actix-web")) {
            return "Actix Web";
        }

        if (containsRustDependency(repositoryDirectory, "rocket")) {
            return "Rocket";
        }

        if (containsRustDependency(repositoryDirectory, "axum")) {
            return "Axum";
        }

        if (containsRustDependency(repositoryDirectory, "warp")) {
            return "Warp";
        }

        return "Unknown";
    }

    private boolean containsSpringBootApplication(File repositoryDirectory) {

        Path root = repositoryDirectory.toPath();

        Path rootPom = root.resolve("pom.xml");

        if (Files.exists(rootPom)
                && isSpringBootProject(root)
                && containsRootSpringBootApplication(root)) {
            return true;
        }

        Path rootGradle = root.resolve("build.gradle");

        if (Files.exists(rootGradle)
                && isSpringBootProject(root)
                && containsRootSpringBootApplication(root)) {
            return true;
        }

        Path rootGradleKts = root.resolve("build.gradle.kts");

        if (Files.exists(rootGradleKts)
                && isSpringBootProject(root)
                && containsRootSpringBootApplication(root)) {
            return true;
        }

        return false;
    }

    private boolean isSpringBootProject(Path root) {

        Path pom = root.resolve("pom.xml");

        if (Files.exists(pom)
                && (fileContains(pom, "spring-boot-starter")
                || fileContains(pom, "spring-boot-maven-plugin")
                || fileContains(pom, "spring-boot-dependencies"))) {
            return true;
        }

        Path gradle = root.resolve("build.gradle");

        if (Files.exists(gradle)
                && (fileContains(gradle, "spring-boot-starter")
                || fileContains(gradle, "org.springframework.boot"))) {
            return true;
        }

        Path gradleKts = root.resolve("build.gradle.kts");

        return Files.exists(gradleKts)
                && (fileContains(gradleKts, "spring-boot-starter")
                || fileContains(gradleKts, "org.springframework.boot"));
    }

    private boolean containsRootSpringBootApplication(Path root) {

        Path srcMainJava =
                root.resolve("src")
                        .resolve("main")
                        .resolve("java");

        if (!Files.exists(srcMainJava)) {
            return false;
        }

        try (Stream<Path> paths = Files.walk(srcMainJava)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .anyMatch(this::isSpringBootApplicationFile);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSpringBootApplicationFile(Path file) {

        try {

            String content = Files.readString(file);

            return content.contains("@SpringBootApplication");

        } catch (Exception e) {
            return false;
        }
    }

    private boolean containsFile(
            File directory,
            String fileName) {

        if (directory == null || !directory.exists()) {
            return false;
        }

        File[] files = directory.listFiles();

        if (files == null) {
            return false;
        }

        for (File file : files) {

            if (file.getName().equalsIgnoreCase(fileName)) {
                return true;
            }
        }

        return false;
    }

    private boolean fileContains(
            Path file,
            String keyword) {

        if (file == null || !Files.exists(file)) {
            return false;
        }

        try (Stream<String> lines = Files.lines(file)) {

            return lines.anyMatch(
                    line -> line
                            .toLowerCase()
                            .contains(keyword.toLowerCase())
            );

        } catch (Exception e) {
            return false;
        }
    }

    private boolean containsPythonDependency(
            File repositoryDirectory,
            String dependency) {

        Path requirements =
                repositoryDirectory.toPath()
                        .resolve("requirements.txt");

        if (Files.exists(requirements)
                && fileContains(requirements, dependency)) {
            return true;
        }

        Path pyproject =
                repositoryDirectory.toPath()
                        .resolve("pyproject.toml");

        if (Files.exists(pyproject)
                && fileContains(pyproject, dependency)) {
            return true;
        }

        Path setup =
                repositoryDirectory.toPath()
                        .resolve("setup.py");

        return Files.exists(setup)
                && fileContains(setup, dependency);
    }

    private boolean containsRubyDependency(
            File repositoryDirectory,
            String dependency) {

        Path gemfile =
                repositoryDirectory.toPath()
                        .resolve("Gemfile");

        return Files.exists(gemfile)
                && fileContains(gemfile, dependency);
    }

    private boolean containsPhpDependency(
            File repositoryDirectory,
            String dependency) {

        Path composer =
                repositoryDirectory.toPath()
                        .resolve("composer.json");

        return Files.exists(composer)
                && fileContains(composer, dependency);
    }

    private boolean containsGoDependency(
            File repositoryDirectory,
            String dependency) {

        Path goMod =
                repositoryDirectory.toPath()
                        .resolve("go.mod");

        return Files.exists(goMod)
                && fileContains(goMod, dependency);
    }

    private boolean containsRustDependency(
            File repositoryDirectory,
            String dependency) {

        Path cargo =
                repositoryDirectory.toPath()
                        .resolve("Cargo.toml");

        return Files.exists(cargo)
                && fileContains(cargo, dependency);
    }
}