package com.repolens.backend.analyzer;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class DependencyAnalyzer {

    public List<String> detectDependencies(File repositoryDirectory) {

        List<String> dependencies = new ArrayList<>();

        if (repositoryDirectory == null || !repositoryDirectory.exists()) {
            return dependencies;
        }

        try (Stream<Path> paths =
                     Files.walk(repositoryDirectory.toPath())) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                    .toString()
                                    .equalsIgnoreCase("pom.xml"))
                    .forEach(path ->
                            analyzePom(path.toFile(), dependencies));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dependencies;
    }

    private void analyzePom(
            File pomFile,
            List<String> dependencies) {

        try {

            Document document =
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse(pomFile);

            document.getDocumentElement().normalize();

            NodeList dependencyNodes =
                    document.getElementsByTagName("dependency");

            for (int i = 0;
                 i < dependencyNodes.getLength();
                 i++) {

                Node dependencyNode =
                        dependencyNodes.item(i);

                if (isInsideDependencyManagement(
                        dependencyNode)) {
                    continue;
                }

                String artifact =
                        getArtifactId(dependencyNode);

                if (artifact != null) {
                    mapDependency(
                            artifact.toLowerCase(),
                            dependencies);
                }
            }

        } catch (Exception e) {
            System.err.println(
                    "Failed to analyze pom: "
                            + pomFile.getAbsolutePath()
            );
        }
    }

    private String getArtifactId(Node dependencyNode) {

        NodeList children =
                dependencyNode.getChildNodes();

        for (int i = 0;
             i < children.getLength();
             i++) {

            Node child = children.item(i);

            if ("artifactId".equals(child.getNodeName())) {

                return child
                        .getTextContent()
                        .trim();
            }
        }

        return null;
    }

    private boolean isInsideDependencyManagement(
            Node node) {

        Node parent = node.getParentNode();

        while (parent != null) {

            if ("dependencyManagement"
                    .equals(parent.getNodeName())) {

                return true;
            }

            parent = parent.getParentNode();
        }

        return false;
    }

    private void mapDependency(
            String artifact,
            List<String> dependencies) {

        addDependency(
                artifact,
                "spring-boot",
                "Spring Boot",
                dependencies
        );

        addDependency(
                artifact,
                "spring-data-jpa",
                "Spring Data JPA",
                dependencies
        );

        addDependency(
                artifact,
                "postgresql",
                "PostgreSQL",
                dependencies
        );

        addDependency(
                artifact,
                "mysql",
                "MySQL",
                dependencies
        );

        addDependency(
                artifact,
                "thymeleaf",
                "Thymeleaf",
                dependencies
        );

        addDependency(
                artifact,
                "lombok",
                "Lombok",
                dependencies
        );

        addDependency(
                artifact,
                "validation",
                "Spring Validation",
                dependencies
        );

        addDependency(
                artifact,
                "security",
                "Spring Security",
                dependencies
        );

        addDependency(
                artifact,
                "redis",
                "Redis",
                dependencies
        );

        addDependency(
                artifact,
                "mongodb",
                "MongoDB",
                dependencies
        );

        addDependency(
                artifact,
                "kafka",
                "Apache Kafka",
                dependencies
        );
    }

    private void addDependency(
            String artifact,
            String keyword,
            String displayName,
            List<String> dependencies) {

        if (artifact.contains(keyword)
                && !dependencies.contains(displayName)) {

            dependencies.add(displayName);
        }
    }
}