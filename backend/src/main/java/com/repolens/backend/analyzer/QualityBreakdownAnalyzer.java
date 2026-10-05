package com.repolens.backend.analyzer;

import com.repolens.backend.dto.CodeQualityInfo;
import com.repolens.backend.dto.DocumentationInfo;
import com.repolens.backend.dto.DuplicationInfo;
import com.repolens.backend.dto.HealthScore;
import com.repolens.backend.dto.QualityBreakdown;
import com.repolens.backend.dto.SecurityInfo;
import org.springframework.stereotype.Component;

@Component
public class QualityBreakdownAnalyzer {

    public QualityBreakdown analyze(
            HealthScore healthScore,
            SecurityInfo security,
            CodeQualityInfo codeQuality,
            DuplicationInfo duplication,
            DocumentationInfo documentation) {

        int securityScore;
        if (security.getIssuesFound() == 0) {
            securityScore = 10;
        } else if (security.getIssuesFound() <= 2) {
            securityScore = 7;
        } else if (security.getIssuesFound() <= 5) {
            securityScore = 5;
        } else {
            securityScore = 2;
        }

        int codeQualityScore;
        switch (codeQuality.getRating().toUpperCase()) {
            case "A":
                codeQualityScore = 10;
                break;
            case "B":
                codeQualityScore = 8;
                break;
            case "C":
                codeQualityScore = 6;
                break;
            default:
                codeQualityScore = 4;
        }

        int documentationScore;
        switch (documentation.getDocumentationScore().toUpperCase()) {
            case "EXCELLENT":
                documentationScore = 10;
                break;
            case "GOOD":
                documentationScore = 7;
                break;
            default:
                documentationScore = 3;
        }

        int projectStructureScore =
                duplication.getDuplicateFiles() == 0 ? 10 : 8;

        return QualityBreakdown.builder()
                .readme(healthScore.isReadme() ? 10 : 0)
                .license(healthScore.isLicense() ? 10 : 0)
                .tests(healthScore.isTests() ? 15 : 0)
                .githubActions(healthScore.isGithubActions() ? 15 : 0)
                .docker(healthScore.isDocker() ? 10 : 0)
                .dockerCompose(healthScore.isDockerCompose() ? 5 : 0)
                .projectStructure(projectStructureScore)
                .security(securityScore)
                .documentation(documentationScore)
                .codeQuality(codeQualityScore)
                .build();
    }
}