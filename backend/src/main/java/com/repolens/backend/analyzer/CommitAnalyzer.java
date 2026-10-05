package com.repolens.backend.analyzer;

import com.repolens.backend.dto.CommitInfo;
import com.repolens.backend.dto.github.CommitApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CommitAnalyzer {

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
        String repo = parts[1];

        String apiUrl =
                "https://api.github.com/repos/"
                        + owner
                        + "/"
                        + repo
                        + "/commits?per_page=1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CommitApiResponse[]> response =
                restTemplate.exchange(
                        apiUrl,
                        HttpMethod.GET,
                        entity,
                        CommitApiResponse[].class
                );

        CommitApiResponse[] commits = response.getBody();

        if (commits == null || commits.length == 0) {
            return null;
        }

        CommitApiResponse latestCommit = commits[0];

        if (latestCommit == null || latestCommit.getCommit() == null) {
            return null;
        }

        CommitApiResponse.Commit commit =
                latestCommit.getCommit();

        CommitApiResponse.Author author =
                commit.getAuthor();

        return CommitInfo.builder()
                .latestCommitMessage(commit.getMessage())
                .latestCommitAuthor(
                        author != null ? author.getName() : null
                )
                .latestCommitDate(
                        author != null ? author.getDate() : null
                )
                .latestCommitSha(latestCommit.getSha())
                .build();
    }
}