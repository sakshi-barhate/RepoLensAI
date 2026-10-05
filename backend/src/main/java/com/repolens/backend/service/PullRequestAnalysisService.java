package com.repolens.backend.service;

import com.repolens.backend.ai.GeminiService;
import com.repolens.backend.ai.PromptBuilder;
import com.repolens.backend.analyzer.PullRequestDiffAnalyzer;
import com.repolens.backend.analyzer.PullRequestSecurityAnalyzer;
import com.repolens.backend.dto.AiReview;
import com.repolens.backend.dto.PullRequestAnalysisResult;
import com.repolens.backend.dto.github.GitHubPullRequestFileResponse;
import com.repolens.backend.dto.github.GitHubPullRequestResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PullRequestAnalysisService {

    private final PullRequestService pullRequestService;
    private final PullRequestDiffAnalyzer pullRequestDiffAnalyzer;
    private final PullRequestSecurityAnalyzer pullRequestSecurityAnalyzer;
    private final PromptBuilder promptBuilder;
    private final GeminiService geminiService;

    public PullRequestAnalysisService(
            PullRequestService pullRequestService,
            PullRequestDiffAnalyzer pullRequestDiffAnalyzer,
            PullRequestSecurityAnalyzer pullRequestSecurityAnalyzer,
            PromptBuilder promptBuilder,
            GeminiService geminiService) {

        this.pullRequestService = pullRequestService;
        this.pullRequestDiffAnalyzer = pullRequestDiffAnalyzer;
        this.pullRequestSecurityAnalyzer = pullRequestSecurityAnalyzer;
        this.promptBuilder = promptBuilder;
        this.geminiService = geminiService;
    }

    public PullRequestAnalysisResult analyzePullRequest(
            String owner,
            String repository,
            int pullRequestNumber) {

        GitHubPullRequestResponse pullRequest =
                pullRequestService.getPullRequest(
                        owner,
                        repository,
                        pullRequestNumber
                );

        List<GitHubPullRequestFileResponse> files =
                pullRequestService.getPullRequestFiles(
                        owner,
                        repository,
                        pullRequestNumber
                );

        int additions = 0;
        int deletions = 0;

        List<String> qualityIssues = new ArrayList<>();
        List<String> securityIssues = new ArrayList<>();

        StringBuilder diffBuilder = new StringBuilder();

        for (GitHubPullRequestFileResponse file : files) {

            additions += file.getAdditions();
            deletions += file.getDeletions();

            String patch = file.getPatch();

            List<String> fileQualityIssues =
                    pullRequestDiffAnalyzer.analyzePatch(patch);

            for (String issue : fileQualityIssues) {
                qualityIssues.add(
                        file.getFilename() + ": " + issue
                );
            }

            List<String> fileSecurityIssues =
                    pullRequestSecurityAnalyzer.analyzePatch(patch);

            for (String issue : fileSecurityIssues) {
                securityIssues.add(
                        file.getFilename() + ": " + issue
                );
            }

            if (patch != null && !patch.isBlank()) {
                diffBuilder
                        .append("\n--- ")
                        .append(file.getFilename())
                        .append(" ---\n")
                        .append(patch)
                        .append("\n");
            }
        }

        String prompt =
                promptBuilder.buildPullRequestReviewPrompt(
                        pullRequest.getTitle(),
                        pullRequest.getUser() != null
                                ? pullRequest.getUser().getLogin()
                                : "Unknown",
                        pullRequest.getState(),
                        diffBuilder.toString(),
                        securityIssues.toString(),
                        qualityIssues.toString()
                );

        String aiReviewText =
                geminiService.generateReview(prompt);

        AiReview aiReview =
                parseAiReview(aiReviewText);

        return PullRequestAnalysisResult.builder()
                .pullRequestNumber(pullRequest.getNumber())
                .title(pullRequest.getTitle())
                .author(
                        pullRequest.getUser() != null
                                ? pullRequest.getUser().getLogin()
                                : "Unknown"
                )
                .state(pullRequest.getState())
                .changedFiles(files.size())
                .additions(additions)
                .deletions(deletions)
                .totalChanges(additions + deletions)
                .securityIssues(securityIssues)
                .qualityIssues(qualityIssues)
                .aiReview(aiReview)
                .build();
    }

    private AiReview parseAiReview(String review) {

        if (review == null || review.isBlank()) {
            return AiReview.builder()
                    .summary("AI review unavailable.")
                    .security("AI review unavailable.")
                    .codeQuality("AI review unavailable.")
                    .recommendations("AI review unavailable.")
                    .overallReview("AI review unavailable.")
                    .build();
        }

        String summary =
                extractSection(review, "1. Summary", "2. Security");

        String security =
                extractSection(review, "2. Security", "3. Code Quality");

        String codeQuality =
                extractSection(review, "3. Code Quality", "4. Recommendations");

        String recommendations =
                extractSection(review, "4. Recommendations", "5. Overall Review");

        String overallReview =
                extractSection(review, "5. Overall Review", null);

        return AiReview.builder()
                .summary(summary)
                .security(security)
                .codeQuality(codeQuality)
                .recommendations(recommendations)
                .overallReview(overallReview)
                .build();
    }

    private String extractSection(
            String text,
            String startMarker,
            String endMarker) {

        int startIndex = text.indexOf(startMarker);

        if (startIndex == -1) {
            return "";
        }

        startIndex += startMarker.length();

        int endIndex;

        if (endMarker == null) {
            endIndex = text.length();
        } else {
            endIndex = text.indexOf(endMarker, startIndex);

            if (endIndex == -1) {
                endIndex = text.length();
            }
        }

        return text
                .substring(startIndex, endIndex)
                .trim();
    }
}