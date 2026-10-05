package com.repolens.backend.analyzer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PullRequestSecurityAnalyzer {

    private static final Pattern HARDCODED_CREDENTIAL =
            Pattern.compile(
                    "(?i)\\b(password|passwd|secret|api[_-]?key|access[_-]?key)"
                            + "\\b\\s*[:=]\\s*[\"']([^\"']+)[\"']"
            );

    private static final Pattern SQL_CONCATENATION =
            Pattern.compile(
                    "(?i)(select|insert|update|delete)\\s+.*[\"']\\s*\\+\\s*[a-zA-Z_$]"
            );

    private static final Pattern WEAK_HASH =
            Pattern.compile("(?i)\\b(md5|sha1|sha-1)\\b");

    public List<String> analyzePatch(String patch) {

        List<String> issues = new ArrayList<>();

        if (patch == null || patch.isBlank()) {
            return issues;
        }

        for (String line : patch.split("\n")) {

            // Analyze only newly added lines.
            if (!line.startsWith("+") || line.startsWith("+++")) {
                continue;
            }

            String addedLine = line.substring(1).trim();

            if (addedLine.isBlank()) {
                continue;
            }

            // Hardcoded credentials
            if (HARDCODED_CREDENTIAL.matcher(addedLine).find()) {
                issues.add("Possible hardcoded credential detected");
            }

            // Possible SQL injection
            if (SQL_CONCATENATION.matcher(addedLine).find()) {
                issues.add("Possible SQL injection risk detected");
            }

            // Runtime command execution
            if (addedLine.contains("Runtime.getRuntime().exec")) {
                issues.add("Runtime.exec() usage detected");
            }

            // Process execution
            if (addedLine.contains("ProcessBuilder")) {
                issues.add("ProcessBuilder usage detected");
            }

            // Weak hashing
            if (WEAK_HASH.matcher(addedLine).find()) {
                issues.add("Weak hashing algorithm detected");
            }
        }

        return issues.stream()
                .distinct()
                .toList();
    }
}