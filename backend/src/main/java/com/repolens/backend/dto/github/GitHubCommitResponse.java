package com.repolens.backend.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitHubCommitResponse {

    private Commit commit;

    @Data
    public static class Commit {

        private Author author;

        private String message;
    }

    @Data
    public static class Author {

        private String name;

        private String date;
    }

    @JsonProperty("sha")
    private String sha;
}