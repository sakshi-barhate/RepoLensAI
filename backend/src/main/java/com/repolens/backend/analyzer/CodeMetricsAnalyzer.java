package com.repolens.backend.analyzer;

import com.repolens.backend.dto.CodeMetrics;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Component
public class CodeMetricsAnalyzer {

    public CodeMetrics analyze(File repositoryDirectory) {

        AtomicInteger classes = new AtomicInteger();
        AtomicInteger interfaces = new AtomicInteger();
        AtomicInteger enums = new AtomicInteger();
        AtomicInteger records = new AtomicInteger();
        AtomicInteger packages = new AtomicInteger();

        scan(
                repositoryDirectory,
                classes,
                interfaces,
                enums,
                records,
                packages
        );

        return CodeMetrics.builder()
                .classes(classes.get())
                .interfaces(interfaces.get())
                .enums(enums.get())
                .records(records.get())
                .packages(packages.get())
                .build();
    }

    private void scan(
            File file,
            AtomicInteger classes,
            AtomicInteger interfaces,
            AtomicInteger enums,
            AtomicInteger records,
            AtomicInteger packages) {

        if (file == null || !file.exists()) {
            return;
        }

        // =========================================================
        // DIRECTORY
        // =========================================================

        if (file.isDirectory()) {

            String name = file.getName();

            if (shouldIgnoreDirectory(name)) {
                return;
            }

            File[] children = file.listFiles();

            if (children != null) {

                for (File child : children) {

                    scan(
                            child,
                            classes,
                            interfaces,
                            enums,
                            records,
                            packages
                    );
                }
            }

            return;
        }

        String fileName = file.getName().toLowerCase();

        try {

            String content = Files.readString(file.toPath());

            if (content.isBlank()) {
                return;
            }

            // =====================================================
            // JAVA
            // =====================================================

            if (fileName.endsWith(".java")) {

                packages.addAndGet(
                        count(
                                content,
                                "\\bpackage\\s+[A-Za-z_$][A-Za-z0-9_$.]*\\s*;"
                        )
                );

                classes.addAndGet(
                        count(
                                content,
                                "\\bclass\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                        )
                );

                interfaces.addAndGet(
                        count(
                                content,
                                "\\binterface\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                        )
                );

                enums.addAndGet(
                        count(
                                content,
                                "\\benum\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                        )
                );

                records.addAndGet(
                        count(
                                content,
                                "\\brecord\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                        )
                );
            }

            // =====================================================
            // C
            // =====================================================

            else if (fileName.endsWith(".c")
                    || fileName.endsWith(".h")) {

                /*
                 * C has:
                 *
                 * - No classes
                 * - No interfaces
                 * - No Java-style packages
                 * - No records
                 *
                 * Therefore:
                 *
                 * classes     = 0
                 * interfaces  = 0
                 * packages    = 0
                 * records     = 0
                 *
                 * We only count enum declarations.
                 */

                enums.addAndGet(
                        count(
                                content,
                                "\\benum\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );
            }

            // =====================================================
            // C++
            // =====================================================

            else if (fileName.endsWith(".cpp")
                    || fileName.endsWith(".cc")
                    || fileName.endsWith(".cxx")
                    || fileName.endsWith(".hpp")) {

                classes.addAndGet(
                        count(
                                content,
                                "\\bclass\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );

                enums.addAndGet(
                        count(
                                content,
                                "\\benum\\s+(class\\s+)?[A-Za-z_][A-Za-z0-9_]*"
                        )
                );
            }

            // =====================================================
            // PYTHON
            // =====================================================

            else if (fileName.endsWith(".py")) {

                classes.addAndGet(
                        count(
                                content,
                                "(?m)^\\s*class\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );
            }

            // =====================================================
            // JAVASCRIPT / TYPESCRIPT
            // =====================================================

            else if (fileName.endsWith(".js")
                    || fileName.endsWith(".jsx")
                    || fileName.endsWith(".ts")
                    || fileName.endsWith(".tsx")) {

                classes.addAndGet(
                        count(
                                content,
                                "\\bclass\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                        )
                );

                /*
                 * Interfaces and enums are mainly TypeScript concepts.
                 * JavaScript files should not normally contribute them.
                 */

                if (fileName.endsWith(".ts")
                        || fileName.endsWith(".tsx")) {

                    interfaces.addAndGet(
                            count(
                                    content,
                                    "\\binterface\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                            )
                    );

                    enums.addAndGet(
                            count(
                                    content,
                                    "\\benum\\s+[A-Za-z_$][A-Za-z0-9_$]*"
                            )
                    );
                }
            }

            // =====================================================
            // C#
            // =====================================================

            else if (fileName.endsWith(".cs")) {

                classes.addAndGet(
                        count(
                                content,
                                "\\bclass\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );

                interfaces.addAndGet(
                        count(
                                content,
                                "\\binterface\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );

                enums.addAndGet(
                        count(
                                content,
                                "\\benum\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );
            }

            // =====================================================
            // GO
            // =====================================================

            else if (fileName.endsWith(".go")) {

                interfaces.addAndGet(
                        count(
                                content,
                                "\\btype\\s+[A-Za-z_][A-Za-z0-9_]*\\s+interface\\s*\\{"
                        )
                );

                classes.addAndGet(
                        count(
                                content,
                                "\\btype\\s+[A-Za-z_][A-Za-z0-9_]*\\s+struct\\s*\\{"
                        )
                );
            }

            // =====================================================
            // RUST
            // =====================================================

            else if (fileName.endsWith(".rs")) {

                classes.addAndGet(
                        count(
                                content,
                                "\\bstruct\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );

                interfaces.addAndGet(
                        count(
                                content,
                                "\\btrait\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );

                enums.addAndGet(
                        count(
                                content,
                                "\\benum\\s+[A-Za-z_][A-Za-z0-9_]*"
                        )
                );
            }

        } catch (Exception ignored) {

            // Ignore files that cannot be read
        }
    }

    // =============================================================
    // IGNORED DIRECTORIES
    // =============================================================

    private boolean shouldIgnoreDirectory(String name) {

        return name.equals("node_modules")
                || name.equals("target")
                || name.equals("build")
                || name.equals(".git")
                || name.equals("dist")
                || name.equals("vendor")
                || name.equals(".idea")
                || name.equals(".vscode")
                || name.equals("bin")
                || name.equals("obj")
                || name.equals("out")
                || name.equals("coverage")
                || name.equals(".gradle");
    }

    // =============================================================
    // REGEX COUNTER
    // =============================================================

    private int count(String content, String regex) {

        if (content == null || content.isBlank()) {
            return 0;
        }

        return (int) Pattern
                .compile(regex)
                .matcher(content)
                .results()
                .count();
    }
}