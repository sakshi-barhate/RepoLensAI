package com.repolens.backend.analyzer;

import com.repolens.backend.dto.SecurityInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SecurityAnalyzer {

    /*
     * Detects credential-related configuration keys
     * with an explicitly assigned value.
     *
     * Examples:
     *
     * password: "admin123"
     * secret: "mySecret"
     * apiKey: "AIza..."
     * access_key = "abc123"
     */
    private static final Pattern HARDCODED_CREDENTIAL =
            Pattern.compile(
                    "(?i)\\b(password|passwd|secret|api[_-]?key|access[_-]?key)"
                            + "\\b\\s*[:=]\\s*[\"']([^\"']+)[\"']"
            );

    /*
     * Detect SQL queries built using string concatenation.
     */
    private static final Pattern SQL_CONCATENATION =
            Pattern.compile(
                    "(?i)(select|insert|update|delete)\\s+.*[\"']\\s*\\+\\s*[a-zA-Z_$]"
            );

    /*
     * Detect weak hashing algorithms.
     */
    private static final Pattern WEAK_HASH =
            Pattern.compile(
                    "(?i)\\b(md5|sha1|sha-1)\\b"
            );

    /*
     * Directories that should not be analyzed.
     */
    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(
            ".git",
            ".idea",
            ".vscode",
            "target",
            "build",
            "dist",
            "node_modules",
            ".next",
            ".gradle",
            "vendor",
            "bin",
            "obj",
            "out"
    );

    /*
     * Analyze repository security.
     */
    public SecurityInfo analyze(File repositoryDirectory) {

        List<String> issues = new ArrayList<>();

        scan(
                repositoryDirectory,
                repositoryDirectory,
                issues
        );

        String risk;

        if (issues.size() >= 5) {
            risk = "High";
        } else if (issues.size() >= 2) {
            risk = "Medium";
        } else {
            risk = "Low";
        }

        return SecurityInfo.builder()
                .risk(risk)
                .issuesFound(issues.size())
                .issues(issues)
                .build();
    }

    /*
     * Recursively scan repository files.
     */
    private void scan(
            File file,
            File repositoryRoot,
            List<String> issues) {

        if (file == null || !file.exists()) {
            return;
        }

        /*
         * Directory handling.
         */
        if (file.isDirectory()) {

            String directoryName =
                    file.getName().toLowerCase();

            if (EXCLUDED_DIRECTORIES.contains(directoryName)) {
                return;
            }

            File[] children = file.listFiles();

            if (children != null) {

                for (File child : children) {

                    scan(
                            child,
                            repositoryRoot,
                            issues
                    );
                }
            }

            return;
        }

        String name =
                file.getName().toLowerCase();

        /*
         * Ignore test files for production security scoring.
         */
        if (name.endsWith("test.java")
                || name.endsWith("tests.java")) {

            return;
        }

        /*
         * Only inspect relevant source/configuration files.
         */
        if (!(name.endsWith(".java")
                || name.endsWith(".properties")
                || name.endsWith(".yml")
                || name.endsWith(".yaml")
                || name.endsWith(".env")
                || name.endsWith(".xml")
                || name.equals("dockerfile")
                || name.equals("docker-compose.yml")
                || name.equals("docker-compose.yaml"))) {

            return;
        }

        try {

            String content =
                    Files.readString(file.toPath());

            /*
             * Remove comments before security checks.
             */
            String cleanContent =
                    removeComments(content);

            /*
             * Get repository-relative file path.
             *
             * Example:
             * k8s/db.yml
             */
            String relativePath =
                    getRelativePath(
                            repositoryRoot,
                            file
                    );

            /*
             * 1. Hardcoded credentials
             */
            if (containsHardcodedCredential(cleanContent)) {

                issues.add(
                        "Possible hardcoded credential in "
                                + relativePath
                );
            }

            /*
             * 2. SQL injection
             */
            if (SQL_CONCATENATION
                    .matcher(cleanContent)
                    .find()) {

                issues.add(
                        "Possible SQL injection risk in "
                                + relativePath
                );
            }

            /*
             * 3. Dangerous command execution
             */
            if (cleanContent.contains(
                    "Runtime.getRuntime().exec")) {

                issues.add(
                        "Runtime.exec() used in "
                                + relativePath
                );
            }

            if (cleanContent.contains(
                    "ProcessBuilder")) {

                issues.add(
                        "ProcessBuilder used in "
                                + relativePath
                );
            }

            /*
             * 4. Weak hashing
             */
            if (WEAK_HASH
                    .matcher(cleanContent)
                    .find()) {

                issues.add(
                        "Weak hashing algorithm used in "
                                + relativePath
                );
            }

        } catch (Exception ignored) {

            /*
             * Ignore files that cannot be read.
             */
        }
    }

    /*
     * Detect whether a real hardcoded credential exists.
     */
    private boolean containsHardcodedCredential(
            String content) {

        Matcher matcher =
                HARDCODED_CREDENTIAL.matcher(content);

        while (matcher.find()) {

            String value =
                    matcher.group(2).trim();

            /*
             * Ignore placeholders, masked values
             * and environment variables.
             */
            if (isIgnoredCredentialValue(value)) {
                continue;
            }

            /*
             * A real non-placeholder value was found.
             */
            return true;
        }

        return false;
    }

    /*
     * Determine whether a credential-like value
     * should be ignored.
     */
    private boolean isIgnoredCredentialValue(
            String value) {

        if (value == null || value.isBlank()) {
            return true;
        }

        String normalized =
                value.trim().toLowerCase();

        /*
         * Environment/configuration placeholders.
         *
         * Examples:
         *
         * ${DB_PASSWORD}
         * ${API_KEY}
         * $PASSWORD
         * $(PASSWORD)
         */
        if (value.startsWith("${")
                || value.startsWith("$(")
                || value.startsWith("$")) {

            return true;
        }

        /*
         * Values containing unresolved
         * environment placeholders.
         */
        if (value.contains("${")) {
            return true;
        }

        /*
         * Masked credentials.
         *
         * Examples:
         *
         * ****
         * ***
         * ******
         * xxxxx
         * XXXXX
         */
        if (normalized.matches("[*xX]+")) {
            return true;
        }

        /*
         * Placeholder values enclosed in
         * angle brackets.
         *
         * Examples:
         *
         * <password>
         * <secret>
         * <api-key>
         */
        if (normalized.startsWith("<")
                && normalized.endsWith(">")) {

            return true;
        }

        /*
         * Common non-secret values.
         */
        if (normalized.equals("null")
                || normalized.equals("true")
                || normalized.equals("false")
                || normalized.equals("password")
                || normalized.equals("secret")
                || normalized.equals("none")
                || normalized.equals("default")) {

            return true;
        }

        /*
         * Common sample/placeholder values.
         */
        if (normalized.equals("changeme")
                || normalized.equals("change-me")
                || normalized.equals("change_me")
                || normalized.equals("your-password")
                || normalized.equals("your_password")
                || normalized.equals("yourpassword")
                || normalized.equals("your-secret")
                || normalized.equals("your_secret")
                || normalized.equals("yoursecret")
                || normalized.equals("your-api-key")
                || normalized.equals("your_api_key")
                || normalized.equals("yourapikey")
                || normalized.equals("your-access-key")
                || normalized.equals("your_access_key")
                || normalized.equals("youraccesskey")
                || normalized.equals("replace-me")
                || normalized.equals("replace_me")
                || normalized.equals("example")
                || normalized.equals("example-password")
                || normalized.equals("example_password")
                || normalized.equals("example-secret")
                || normalized.equals("example_secret")
                || normalized.equals("sample")
                || normalized.equals("sample-password")
                || normalized.equals("sample-password-value")) {

            return true;
        }

        return false;
    }

    /*
     * Convert an absolute file path into a path
     * relative to the cloned repository.
     */
    private String getRelativePath(
            File repositoryRoot,
            File file) {

        try {

            return repositoryRoot
                    .toPath()
                    .relativize(file.toPath())
                    .toString()
                    .replace(File.separatorChar, '/');

        } catch (Exception ignored) {

            return file.getName();
        }
    }

    /*
     * Remove comments before performing security analysis.
     */
    private String removeComments(
            String content) {

        /*
         * Remove Java/C/JavaScript-style
         * single-line comments.
         */
        content = content.replaceAll(
                "(?m)//.*$",
                ""
        );

        /*
         * Remove block comments.
         */
        content = content.replaceAll(
                "(?s)/\\*.*?\\*/",
                ""
        );

        /*
         * Remove YAML/Python-style comments.
         */
        content = content.replaceAll(
                "(?m)#.*$",
                ""
        );

        return content;
    }
}