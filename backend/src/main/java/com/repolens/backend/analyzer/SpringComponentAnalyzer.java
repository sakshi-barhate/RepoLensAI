package com.repolens.backend.analyzer;

import com.repolens.backend.dto.SpringComponents;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SpringComponentAnalyzer {

    public SpringComponents analyze(File repositoryDirectory) {

        AtomicInteger controllers = new AtomicInteger();
        AtomicInteger restControllers = new AtomicInteger();
        AtomicInteger services = new AtomicInteger();
        AtomicInteger repositories = new AtomicInteger();
        AtomicInteger entities = new AtomicInteger();
        AtomicInteger components = new AtomicInteger();
        AtomicInteger configurations = new AtomicInteger();

        scan(repositoryDirectory,
                controllers,
                restControllers,
                services,
                repositories,
                entities,
                components,
                configurations);

        return SpringComponents.builder()
                .controllers(controllers.get())
                .restControllers(restControllers.get())
                .services(services.get())
                .repositories(repositories.get())
                .entities(entities.get())
                .components(components.get())
                .configurations(configurations.get())
                .build();
    }

    private void scan(File file,
                      AtomicInteger controllers,
                      AtomicInteger restControllers,
                      AtomicInteger services,
                      AtomicInteger repositories,
                      AtomicInteger entities,
                      AtomicInteger components,
                      AtomicInteger configurations) {

        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            File[] children = file.listFiles();

            if (children != null) {
                for (File child : children) {
                    scan(child,
                            controllers,
                            restControllers,
                            services,
                            repositories,
                            entities,
                            components,
                            configurations);
                }
            }
            return;
        }

        if (!file.getName().endsWith(".java")) {
            return;
        }

        try {

            String content = Files.readString(file.toPath());

            if (content.contains("@RestController")) {
                restControllers.incrementAndGet();
            }

            if (content.contains("@Controller")) {
                controllers.incrementAndGet();
            }

            if (content.contains("@Service")) {
                services.incrementAndGet();
            }

            if (content.contains("@Repository")) {
                repositories.incrementAndGet();
            }

            if (content.contains("@Entity")) {
                entities.incrementAndGet();
            }

            if (content.contains("@Component")) {
                components.incrementAndGet();
            }

            if (content.contains("@Configuration")) {
                configurations.incrementAndGet();
            }

        } catch (Exception ignored) {
        }
    }
}