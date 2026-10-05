package com.repolens.backend.analyzer;

import com.repolens.backend.dto.CommitInfo;
import com.repolens.backend.dto.github.GitHubCommitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GitHubCommitAnalyzer {

    @Autowired
    private RestTemplate restTemplate;

    public CommitInfo analyze(String repositoryUrl) {

        String[] parts = repositoryUrl
                .replace("https://github.com/", "")
                .replace(".git", "")
                .split("/");

        if (parts.length < 2) {
            return null;
        }

        String owner = parts[0];
        String repository = parts[1];

        String apiUrl =
                "https://api.github.com/repos/"
                        + owner
                        + "/"
                        + repository
                        + "/commits?per_page=1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GitHubCommitResponse[]> response =
                restTemplate.exchange(
                        apiUrl,
                        HttpMethod.GET,
                        entity,
                        GitHubCommitResponse[].class
                );

        GitHubCommitResponse[] commits = response.getBody();

        if (commits == null || commits.length == 0) {
            return null;
        }

        GitHubCommitResponse latest = commits[0];

        return CommitInfo.builder()
                .latestCommitMessage(latest.getCommit().getMessage())
                .latestCommitAuthor(latest.getCommit().getAuthor().getName())
                .latestCommitDate(latest.getCommit().getAuthor().getDate())
                .latestCommitSha(latest.getSha())
                .build();
    }
}