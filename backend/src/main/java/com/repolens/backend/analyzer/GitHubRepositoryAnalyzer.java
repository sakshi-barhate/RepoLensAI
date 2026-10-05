package com.repolens.backend.analyzer;

import com.repolens.backend.dto.GitHubRepositoryInfo;
import com.repolens.backend.dto.github.GitHubApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class GitHubRepositoryAnalyzer {

    @Autowired
    private RestTemplate restTemplate;

    public GitHubRepositoryInfo analyze(String repositoryUrl) {

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
                "https://api.github.com/repos/" + owner + "/" + repository;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            ResponseEntity<GitHubApiResponse> response =
                    restTemplate.exchange(
                            apiUrl,
                            HttpMethod.GET,
                            entity,
                            GitHubApiResponse.class
                    );

            GitHubApiResponse body = response.getBody();

            if (body == null) {
                return null;
            }

            return GitHubRepositoryInfo.builder()
                    .owner(body.getOwner().getLogin())
                    .repository(body.getName())
                    .description(body.getDescription())
                    .defaultBranch(body.getDefaultBranch())
                    .stars(body.getStargazersCount())
                    .forks(body.getForksCount())
                    .watchers(body.getSubscribersCount())
                    .openIssues(body.getOpenIssuesCount())
                    .lastUpdated(body.getUpdatedAt())
                    .homepage(body.getHomepage())
                    .topics(body.getTopics())
                    .build();

        } catch (HttpClientErrorException e) {

            System.err.println(
                    "GitHub API unavailable: " + e.getStatusCode()
            );

            return null;

        } catch (Exception e) {

            System.err.println(
                    "GitHub API request failed: " + e.getMessage()
            );

            return null;
        }
    }

    public boolean repositoryExists(String repositoryUrl) {

        String[] parts = repositoryUrl
                .replace("https://github.com/", "")
                .replace(".git", "")
                .split("/");

        if (parts.length < 2) {
            return false;
        }

        String owner = parts[0];
        String repository = parts[1];

        String apiUrl =
                "https://api.github.com/repos/" + owner + "/" + repository;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    GitHubApiResponse.class
            );

            return true;

        } catch (HttpClientErrorException.NotFound e) {

            return false;

        } catch (Exception e) {

            return false;
        }
    }
}