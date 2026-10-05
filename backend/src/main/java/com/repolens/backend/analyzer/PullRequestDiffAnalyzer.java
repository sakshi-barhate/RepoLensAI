package com.repolens.backend.analyzer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PullRequestDiffAnalyzer {

    public List<String> analyzePatch(String patch) {

        List<String> issues = new ArrayList<>();

        if (patch == null || patch.isBlank()) {
            return issues;
        }

        for (String line : patch.split("\n")) {

            // Ignore deleted lines.
            // We only analyze code that is being added by the PR.
            if (!line.startsWith("+") || line.startsWith("+++")) {
                continue;
            }

            String addedLine = line.substring(1).trim();

            if (addedLine.isBlank()) {
                continue;
            }

            // System.out.println()
            if (addedLine.contains("System.out.println(")) {
                issues.add("System.out.println() detected");
            }

            // printStackTrace()
            if (addedLine.contains("printStackTrace(")) {
                issues.add("printStackTrace() detected");
            }

            // Runtime.exec()
            if (addedLine.contains("Runtime.getRuntime().exec")) {
                issues.add("Runtime.exec() detected");
            }

            // ProcessBuilder
            if (addedLine.contains("ProcessBuilder")) {
                issues.add("ProcessBuilder usage detected");
            }

            // TODO
            if (addedLine.toUpperCase().contains("TODO")) {
                issues.add("TODO comment detected");
            }

            // FIXME
            if (addedLine.toUpperCase().contains("FIXME")) {
                issues.add("FIXME comment detected");
            }

            // Empty catch block
            if (addedLine.matches(
                    "catch\\s*\\(.*\\)\\s*\\{\\s*\\}")) {

                issues.add("Empty catch block detected");
            }
        }

        return issues.stream()
                .distinct()
                .toList();
    }
}