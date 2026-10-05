package com.repolens.backend.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GitHubApiResponse {

    private Owner owner;

    private String name;

    private String description;

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("stargazers_count")
    private int stargazersCount;

    @JsonProperty("forks_count")
    private int forksCount;

    @JsonProperty("subscribers_count")
    private int subscribersCount;

    @JsonProperty("open_issues_count")
    private int openIssuesCount;

    @JsonProperty("updated_at")
    private String updatedAt;

    private String homepage;

    private List<String> topics;

    @Data
    public static class Owner {

        private String login;

    }
}