package com.repolens.backend.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitHubPullRequestResponse {

    private int number;

    private String title;

    private String body;

    private String state;

    private User user;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @Data
    public static class User {

        private String login;
    }
}