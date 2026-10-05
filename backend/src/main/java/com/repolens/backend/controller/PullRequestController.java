package com.repolens.backend.controller;

import com.repolens.backend.dto.PullRequestAnalysisResult;
import com.repolens.backend.dto.github.GitHubPullRequestFileResponse;
import com.repolens.backend.dto.github.GitHubPullRequestResponse;
import com.repolens.backend.service.PullRequestAnalysisService;
import com.repolens.backend.service.PullRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(
        origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "https://repo-lens-ai-puce.vercel.app"
        }
)
@RestController
@RequestMapping("/api/pull-request")
public class PullRequestController {

    private final PullRequestService pullRequestService;
    private final PullRequestAnalysisService pullRequestAnalysisService;

    public PullRequestController(
            PullRequestService pullRequestService,
            PullRequestAnalysisService pullRequestAnalysisService) {

        this.pullRequestService = pullRequestService;
        this.pullRequestAnalysisService = pullRequestAnalysisService;
    }

    @GetMapping("/{owner}/{repository}/{number}")
    public GitHubPullRequestResponse getPullRequest(
            @PathVariable String owner,
            @PathVariable String repository,
            @PathVariable int number) {

        return pullRequestService.getPullRequest(
                owner,
                repository,
                number
        );
    }

    @GetMapping("/{owner}/{repository}/{number}/files")
    public List<GitHubPullRequestFileResponse> getPullRequestFiles(
            @PathVariable String owner,
            @PathVariable String repository,
            @PathVariable int number) {

        return pullRequestService.getPullRequestFiles(
                owner,
                repository,
                number
        );
    }

    @GetMapping("/{owner}/{repository}/{number}/analyze")
    public PullRequestAnalysisResult analyzePullRequest(
            @PathVariable String owner,
            @PathVariable String repository,
            @PathVariable int number) {

        return pullRequestAnalysisService.analyzePullRequest(
                owner,
                repository,
                number
        );
    }
}