package com.repolens.backend.analyzer;

import com.repolens.backend.dto.RestApiInfo;
import com.repolens.backend.dto.RestEndpointInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RestApiAnalyzer {

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

    /*
     * Detect both Spring REST controllers and normal Spring MVC controllers.
     */
    private static final Pattern CONTROLLER =
            Pattern.compile(
                    "@RestController\\b|@RestControllerAdvice\\b|@Controller\\b"
            );

    /*
     * Spring shortcut annotations.
     */
    private static final Pattern GET =
            Pattern.compile(
                    "@GetMapping\\s*(?:\\((.*?)\\))?",
                    Pattern.DOTALL
            );

    private static final Pattern POST =
            Pattern.compile(
                    "@PostMapping\\s*(?:\\((.*?)\\))?",
                    Pattern.DOTALL
            );

    private static final Pattern PUT =
            Pattern.compile(
                    "@PutMapping\\s*(?:\\((.*?)\\))?",
                    Pattern.DOTALL
            );

    private static final Pattern DELETE =
            Pattern.compile(
                    "@DeleteMapping\\s*(?:\\((.*?)\\))?",
                    Pattern.DOTALL
            );

    private static final Pattern PATCH =
            Pattern.compile(
                    "@PatchMapping\\s*(?:\\((.*?)\\))?",
                    Pattern.DOTALL
            );

    /*
     * Generic @RequestMapping.
     */
    private static final Pattern REQUEST_MAPPING =
            Pattern.compile(
                    "@RequestMapping\\s*(?:\\((.*?)\\))?",
                    Pattern.DOTALL
            );

    /*
     * Detect method declarations such as:
     *
     * @RequestMapping(method = RequestMethod.GET)
     * @RequestMapping(value = "/owners", method = RequestMethod.POST)
     */
    private static final Pattern REQUEST_METHOD =
            Pattern.compile(
                    "method\\s*=\\s*(?:\\{\\s*)?RequestMethod\\.(GET|POST|PUT|DELETE|PATCH)",
                    Pattern.DOTALL
            );

    /*
     * Extract quoted paths.
     *
     * Examples:
     * "/owners"
     * value = "/owners"
     * path = {"/owners", "/owners/"}
     */
    private static final Pattern PATH =
            Pattern.compile(
                    "\"([^\"]+)\""
            );

    public RestApiInfo analyze(File repositoryDirectory) {

        List<RestEndpointInfo> endpoints =
                new ArrayList<>();

        scan(
                repositoryDirectory,
                endpoints
        );

        int get =
                countByMethod(
                        endpoints,
                        "GET"
                );

        int post =
                countByMethod(
                        endpoints,
                        "POST"
                );

        int put =
                countByMethod(
                        endpoints,
                        "PUT"
                );

        int delete =
                countByMethod(
                        endpoints,
                        "DELETE"
                );

        int patch =
                countByMethod(
                        endpoints,
                        "PATCH"
                );

        return RestApiInfo.builder()
                .getEndpoints(get)
                .postEndpoints(post)
                .putEndpoints(put)
                .deleteEndpoints(delete)
                .patchEndpoints(patch)
                .endpoints(endpoints)
                .build();
    }

    private void scan(
            File file,
            List<RestEndpointInfo> endpoints
    ) {

        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {

            if (EXCLUDED_DIRECTORIES.contains(
                    file.getName().toLowerCase()
            )) {
                return;
            }

            File[] children =
                    file.listFiles();

            if (children != null) {

                for (File child : children) {

                    scan(
                            child,
                            endpoints
                    );
                }
            }

            return;
        }

        if (!file.getName().endsWith(".java")) {
            return;
        }

        try {

            String content =
                    Files.readString(
                            file.toPath()
                    );

            Matcher controllerMatcher =
                    CONTROLLER.matcher(
                            content
                    );

            /*
             * Ignore Java classes that are not controllers.
             */
            if (!controllerMatcher.find()) {
                return;
            }

            /*
             * Extract class-level @RequestMapping.
             */
            String basePath =
                    extractBasePath(
                            content
                    );

            /*
             * First detect shortcut annotations.
             */
            extractShortcutMappings(
                    GET,
                    "GET",
                    content,
                    basePath,
                    endpoints
            );

            extractShortcutMappings(
                    POST,
                    "POST",
                    content,
                    basePath,
                    endpoints
            );

            extractShortcutMappings(
                    PUT,
                    "PUT",
                    content,
                    basePath,
                    endpoints
            );

            extractShortcutMappings(
                    DELETE,
                    "DELETE",
                    content,
                    basePath,
                    endpoints
            );

            extractShortcutMappings(
                    PATCH,
                    "PATCH",
                    content,
                    basePath,
                    endpoints
            );

            /*
             * Then detect generic @RequestMapping methods.
             */
            extractRequestMappings(
                    content,
                    basePath,
                    endpoints
            );

        } catch (Exception ignored) {
            /*
             * Ignore unreadable Java files and continue scanning.
             */
        }
    }

    private String extractBasePath(
            String content
    ) {

        Matcher matcher =
                REQUEST_MAPPING.matcher(
                        content
                );

        if (!matcher.find()) {
            return "";
        }

        String mappingContent =
                matcher.group(1);

        if (mappingContent == null ||
                mappingContent.isBlank()) {

            return "";
        }

        /*
         * Avoid treating method-level RequestMapping
         * declarations as the class-level base path.
         *
         * If RequestMethod appears in the mapping,
         * it is most likely a method-level mapping.
         */
        if (mappingContent.contains(
                "RequestMethod."
        )) {
            return "";
        }

        return extractFirstPath(
                mappingContent
        );
    }

    private void extractShortcutMappings(
            Pattern mappingPattern,
            String method,
            String content,
            String basePath,
            List<RestEndpointInfo> endpoints
    ) {

        Matcher matcher =
                mappingPattern.matcher(
                        content
                );

        while (matcher.find()) {

            String mappingContent =
                    matcher.group(1);

            /*
             * Example:
             *
             * @GetMapping
             *
             * means the method maps to the
             * controller's base path.
             */
            if (mappingContent == null ||
                    mappingContent.isBlank()) {

                String endpoint =
                        combinePaths(
                                basePath,
                                "/"
                        );

                addEndpoint(
                        endpoints,
                        method,
                        endpoint
                );

                continue;
            }

            List<String> paths =
                    extractPaths(
                            mappingContent
                    );

            /*
             * If no path was explicitly supplied,
             * use the controller base path.
             */
            if (paths.isEmpty()) {

                String endpoint =
                        combinePaths(
                                basePath,
                                "/"
                        );

                addEndpoint(
                        endpoints,
                        method,
                        endpoint
                );

                continue;
            }

            for (String path : paths) {

                String endpoint =
                        combinePaths(
                                basePath,
                                path
                        );

                addEndpoint(
                        endpoints,
                        method,
                        endpoint
                );
            }
        }
    }

    private void extractRequestMappings(
            String content,
            String basePath,
            List<RestEndpointInfo> endpoints
    ) {

        Matcher matcher =
                REQUEST_MAPPING.matcher(
                        content
                );

        while (matcher.find()) {

            String mappingContent =
                    matcher.group(1);

            if (mappingContent == null ||
                    mappingContent.isBlank()) {

                continue;
            }

            /*
             * Determine the HTTP method.
             *
             * If no method is specified, Spring's
             * @RequestMapping can handle multiple
             * HTTP methods, so we don't blindly
             * classify it as GET.
             */
            Matcher methodMatcher =
                    REQUEST_METHOD.matcher(
                            mappingContent
                    );

            if (!methodMatcher.find()) {
                continue;
            }

            String method =
                    methodMatcher.group(1);

            List<String> paths =
                    extractPaths(
                            mappingContent
                    );

            if (paths.isEmpty()) {

                String endpoint =
                        combinePaths(
                                basePath,
                                "/"
                        );

                addEndpoint(
                        endpoints,
                        method,
                        endpoint
                );

                continue;
            }

            for (String path : paths) {

                String endpoint =
                        combinePaths(
                                basePath,
                                path
                        );

                addEndpoint(
                        endpoints,
                        method,
                        endpoint
                );
            }
        }
    }

    private List<String> extractPaths(
            String mappingContent
    ) {

        List<String> paths =
                new ArrayList<>();

        Matcher pathMatcher =
                PATH.matcher(
                        mappingContent
                );

        while (pathMatcher.find()) {

            String path =
                    pathMatcher.group(1);

            /*
             * Ignore obvious non-path string values
             * such as RequestMethod names.
             */
            if (path == null ||
                    path.isBlank()) {

                continue;
            }

            /*
             * Only treat strings that look like paths
             * as endpoint paths.
             */
            if (path.startsWith("/")) {

                paths.add(
                        normalize(path)
                );
            }
        }

        return paths;
    }

    private String extractFirstPath(
            String mappingContent
    ) {

        List<String> paths =
                extractPaths(
                        mappingContent
                );

        if (paths.isEmpty()) {
            return "";
        }

        return paths.get(0);
    }

    private String combinePaths(
            String basePath,
            String endpoint
    ) {

        if (endpoint == null ||
                endpoint.isBlank()) {

            endpoint = "/";
        }

        endpoint =
                normalize(
                        endpoint
                );

        if (basePath == null ||
                basePath.isBlank()) {

            return endpoint;
        }

        basePath =
                normalize(
                        basePath
                );

        if (endpoint.equals("/")) {

            return basePath;
        }

        return normalize(
                basePath + "/" + endpoint
        );
    }

    private String normalize(
            String path
    ) {

        if (path == null ||
                path.isBlank()) {

            return "/";
        }

        path =
                path.trim();

        if (!path.startsWith("/")) {

            path =
                    "/" + path;
        }

        path =
                path.replaceAll(
                        "//+",
                        "/"
                );

        if (path.length() > 1 &&
                path.endsWith("/")) {

            path =
                    path.substring(
                            0,
                            path.length() - 1
                    );
        }

        return path;
    }

    private void addEndpoint(
            List<RestEndpointInfo> endpoints,
            String method,
            String path
    ) {

        if (method == null ||
                method.isBlank() ||
                path == null ||
                path.isBlank()) {

            return;
        }

        boolean exists =
                endpoints.stream()
                        .anyMatch(endpoint ->
                                endpoint.getMethod()
                                        .equals(method)
                                        &&
                                endpoint.getPath()
                                        .equals(path)
                        );

        if (!exists) {

            endpoints.add(
                    RestEndpointInfo.builder()
                            .method(method)
                            .path(path)
                            .build()
            );
        }
    }

    private int countByMethod(
            List<RestEndpointInfo> endpoints,
            String method
    ) {

        return (int) endpoints.stream()
                .filter(endpoint ->
                        endpoint.getMethod()
                                .equals(method)
                )
                .count();
    }
}