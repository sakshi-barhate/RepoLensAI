package com.repolens.backend.analyzer;

import com.repolens.backend.dto.BranchInfo;
import com.repolens.backend.dto.github.GitHubApiResponse;
import com.repolens.backend.dto.github.GitHubBranchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class BranchAnalyzer {

    @Autowired
    private RestTemplate restTemplate;

    public BranchInfo analyze(String repositoryUrl) {

        String[] parts = repositoryUrl
                .replace("https://github.com/", "")
                .replace(".git", "")
                .split("/");

        if (parts.length < 2) {
            return null;
        }

        String owner = parts[0];
        String repo = parts[1];

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String repoApi =
                "https://api.github.com/repos/"
                        + owner
                        + "/"
                        + repo;

        ResponseEntity<GitHubApiResponse> repoResponse =
                restTemplate.exchange(
                        repoApi,
                        HttpMethod.GET,
                        entity,
                        GitHubApiResponse.class
                );

        String defaultBranch = null;

        if (repoResponse.getBody() != null) {
            defaultBranch = repoResponse.getBody().getDefaultBranch();
        }

        String branchApi =
                "https://api.github.com/repos/"
                        + owner
                        + "/"
                        + repo
                        + "/branches";

        ResponseEntity<List<GitHubBranchResponse>> branchResponse =
                restTemplate.exchange(
                        branchApi,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<GitHubBranchResponse>>() {}
                );

        List<GitHubBranchResponse> branches = branchResponse.getBody();

        return BranchInfo.builder()
                .defaultBranch(defaultBranch)
                .totalBranches(branches == null ? 0 : branches.size())
                .build();
    }
}