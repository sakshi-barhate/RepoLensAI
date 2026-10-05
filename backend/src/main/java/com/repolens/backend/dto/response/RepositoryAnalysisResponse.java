package com.repolens.backend.dto.response;

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
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RepositoryAnalysisResponse {

    private String repositoryName;

    private String localPath;

    private String language;

    private String buildTool;

    private String framework;

    private List<String> dependencies;

    private List<String> projectStructure;

    private ReadmeInfo readme;

    private RepositoryStatistics statistics;

    private RepositorySizeInfo repositorySize;

    private CodeMetrics codeMetrics;

    private SpringComponents springComponents;

    private RestApiInfo restApi;

    private HealthScore healthScore;

    private QualityBreakdown qualityBreakdown;

    private LicenseInfo licenseInfo;

    private GitHubRepositoryInfo github;

    private BranchInfo branchInfo;

    private CommitInfo commitInfo;

    private ContributorsInfo contributors;

    private ComplexityInfo complexity;

    private SecurityInfo security;

    private DuplicationInfo duplication;

    private CodeQualityInfo codeQuality;

    private DocumentationInfo documentation;

    private String aiSummary;

    private String status;

    private String message;
}