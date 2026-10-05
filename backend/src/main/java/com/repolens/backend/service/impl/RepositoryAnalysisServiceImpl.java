package com.repolens.backend.service.impl;

import com.repolens.backend.analyzer.BranchAnalyzer;
import com.repolens.backend.analyzer.BuildToolDetector;
import com.repolens.backend.analyzer.CodeMetricsAnalyzer;
import com.repolens.backend.analyzer.CodeQualityAnalyzer;
import com.repolens.backend.analyzer.CommitAnalyzer;
import com.repolens.backend.analyzer.ComplexityAnalyzer;
import com.repolens.backend.analyzer.DependencyAnalyzer;
import com.repolens.backend.analyzer.DocumentationAnalyzer;
import com.repolens.backend.analyzer.DuplicationAnalyzer;
import com.repolens.backend.analyzer.FrameworkDetector;
import com.repolens.backend.analyzer.GitHubContributorsAnalyzer;
import com.repolens.backend.analyzer.GitHubRepositoryAnalyzer;
import com.repolens.backend.analyzer.HealthScoreAnalyzer;
import com.repolens.backend.analyzer.LanguageDetector;
import com.repolens.backend.analyzer.LicenseAnalyzer;
import com.repolens.backend.analyzer.ProjectStructureAnalyzer;
import com.repolens.backend.analyzer.QualityBreakdownAnalyzer;
import com.repolens.backend.analyzer.RepositorySizeAnalyzer;
import com.repolens.backend.analyzer.RepositoryStatisticsAnalyzer;
import com.repolens.backend.analyzer.RestApiAnalyzer;
import com.repolens.backend.analyzer.SecurityAnalyzer;
import com.repolens.backend.analyzer.SpringComponentAnalyzer;

import com.repolens.backend.dto.BranchInfo;
import com.repolens.backend.dto.CodeMetrics;
import com.repolens.backend.dto.CodeQualityInfo;
import com.repolens.backend.dto.CommitInfo;
import com.repolens.backend.dto.ComplexityInfo;
import com.repolens.backend.dto.ContributorsInfo;
import com.repolens.backend.dto.DocumentationInfo;
import com.repolens.backend.dto.DuplicationInfo;
import com.repolens.backend.dto.GitHubRepositoryInfo;
import com.repolens.backend.dto.HealthScore;
import com.repolens.backend.dto.LicenseInfo;
import com.repolens.backend.dto.QualityBreakdown;
import com.repolens.backend.dto.ReadmeInfo;
import com.repolens.backend.dto.RepositorySizeInfo;
import com.repolens.backend.dto.RepositoryStatistics;
import com.repolens.backend.dto.RestApiInfo;
import com.repolens.backend.dto.SecurityInfo;
import com.repolens.backend.dto.SpringComponents;

import com.repolens.backend.dto.request.RepositoryAnalysisRequest;
import com.repolens.backend.dto.response.RepositoryAnalysisResponse;

import com.repolens.backend.exception.RepositoryNotFoundException;
import com.repolens.backend.github.RepositoryCloner;
import com.repolens.backend.parser.ReadmeParser;
import com.repolens.backend.service.GeminiService;
import com.repolens.backend.service.RepositoryAnalysisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class RepositoryAnalysisServiceImpl implements RepositoryAnalysisService {

    @Autowired
    private RepositoryCloner repositoryCloner;

    @Autowired
    private LanguageDetector languageDetector;

    @Autowired
    private BuildToolDetector buildToolDetector;

    @Autowired
    private FrameworkDetector frameworkDetector;

    @Autowired
    private DependencyAnalyzer dependencyAnalyzer;

    @Autowired
    private ProjectStructureAnalyzer projectStructureAnalyzer;

    @Autowired
    private RepositoryStatisticsAnalyzer repositoryStatisticsAnalyzer;

    @Autowired
    private RepositorySizeAnalyzer repositorySizeAnalyzer;

    @Autowired
    private CodeMetricsAnalyzer codeMetricsAnalyzer;

    @Autowired
    private SpringComponentAnalyzer springComponentAnalyzer;

    @Autowired
    private RestApiAnalyzer restApiAnalyzer;

    @Autowired
    private HealthScoreAnalyzer healthScoreAnalyzer;

    @Autowired
    private LicenseAnalyzer licenseAnalyzer;

    @Autowired
    private GitHubRepositoryAnalyzer gitHubRepositoryAnalyzer;

    @Autowired
    private BranchAnalyzer branchAnalyzer;

    @Autowired
    private CommitAnalyzer commitAnalyzer;

    @Autowired
    private GitHubContributorsAnalyzer gitHubContributorsAnalyzer;

    @Autowired
    private ComplexityAnalyzer complexityAnalyzer;

    @Autowired
    private SecurityAnalyzer securityAnalyzer;

    @Autowired
    private DuplicationAnalyzer duplicationAnalyzer;

    @Autowired
    private CodeQualityAnalyzer codeQualityAnalyzer;

    @Autowired
    private DocumentationAnalyzer documentationAnalyzer;

    @Autowired
    private ReadmeParser readmeParser;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private QualityBreakdownAnalyzer qualityBreakdownAnalyzer;

    @Override
    public RepositoryAnalysisResponse analyzeRepository(
            RepositoryAnalysisRequest request) {

        String repositoryUrl = request.getRepositoryUrl();

        if (!gitHubRepositoryAnalyzer.repositoryExists(repositoryUrl)) {
            throw new RepositoryNotFoundException(
                    "GitHub repository not found: " + repositoryUrl
            );
        }

        File repository =
                repositoryCloner.cloneRepository(repositoryUrl);

        String language =
                languageDetector.detectLanguage(repository);

        String buildTool =
                buildToolDetector.detectBuildTool(repository);

        String framework =
                frameworkDetector.detectFramework(repository);

        List<String> dependencies =
                dependencyAnalyzer.detectDependencies(repository);

        List<String> projectStructure =
                projectStructureAnalyzer.detectProjectStructure(repository);

        ReadmeInfo readme =
                readmeParser.extractReadme(repository);

        RepositoryStatistics statistics =
                repositoryStatisticsAnalyzer.analyze(repository);

        RepositorySizeInfo repositorySize =
                repositorySizeAnalyzer.analyze(repository);

        CodeMetrics codeMetrics =
                codeMetricsAnalyzer.analyze(repository);

        SpringComponents springComponents = null;

        if ("Spring Boot".equals(framework)) {
            springComponents =
                    springComponentAnalyzer.analyze(repository);
        }

        RestApiInfo restApi =
                restApiAnalyzer.analyze(repository);

        HealthScore healthScore =
                healthScoreAnalyzer.analyze(repository);

        LicenseInfo licenseInfo =
                licenseAnalyzer.analyze(repository);

        GitHubRepositoryInfo github =
                gitHubRepositoryAnalyzer.analyze(repositoryUrl);

        BranchInfo branchInfo =
                branchAnalyzer.analyze(repositoryUrl);

        CommitInfo commitInfo =
                commitAnalyzer.analyze(repositoryUrl);

        ContributorsInfo contributors =
                gitHubContributorsAnalyzer.analyze(repositoryUrl);

        ComplexityInfo complexity =
                complexityAnalyzer.analyze(repository);

        SecurityInfo security =
                securityAnalyzer.analyze(repository);

        DuplicationInfo duplication =
                duplicationAnalyzer.analyze(repository);

        CodeQualityInfo codeQuality =
                codeQualityAnalyzer.analyze(repository);

        DocumentationInfo documentation =
                documentationAnalyzer.analyze(repository);

        QualityBreakdown qualityBreakdown =
                qualityBreakdownAnalyzer.analyze(
                        healthScore,
                        security,
                        codeQuality,
                        duplication,
                        documentation);

        String prompt = """
                Summarize this GitHub repository in 4-5 concise sentences.

                Use ONLY the provided repository metadata.
                Do not invent technologies, frameworks, or architecture.
                If Framework is Unknown, do not claim that the repository primarily uses any specific framework.
                Dependencies may come from multiple modules, so mention them only as technologies present in the repository.
                Clearly distinguish between the detected framework and dependencies.

                Repository Name: %s
                Language: %s
                Build Tool: %s
                Framework: %s
                Dependencies: %s
                README Title: %s
                README Description: %s
                """
                .formatted(
                        repository.getName(),
                        language,
                        buildTool,
                        framework,
                        dependencies,
                        readme.getTitle(),
                        readme.getDescription());

        String aiSummary =
                geminiService.generateRepositorySummary(prompt);

        return RepositoryAnalysisResponse.builder()
                .repositoryName(repository.getName())
                .localPath(repository.getAbsolutePath())
                .language(language)
                .buildTool(buildTool)
                .framework(framework)
                .dependencies(dependencies)
                .projectStructure(projectStructure)
                .readme(readme)
                .statistics(statistics)
                .repositorySize(repositorySize)
                .codeMetrics(codeMetrics)
                .springComponents(springComponents)
                .restApi(restApi)
                .healthScore(healthScore)
                .qualityBreakdown(qualityBreakdown)
                .licenseInfo(licenseInfo)
                .github(github)
                .branchInfo(branchInfo)
                .commitInfo(commitInfo)
                .contributors(contributors)
                .complexity(complexity)
                .security(security)
                .duplication(duplication)
                .codeQuality(codeQuality)
                .documentation(documentation)
                .aiSummary(aiSummary)
                .status("SUCCESS")
                .message("Repository analyzed successfully")
                .build();
    }
}