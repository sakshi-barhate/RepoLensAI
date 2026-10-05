package com.repolens.backend.dto.github;

import lombok.Data;

@Data
public class CommitApiResponse {

    private String sha;

    private Commit commit;

    @Data
    public static class Commit {

        private String message;

        private Author author;
    }

    @Data
    public static class Author {

        private String name;

        private String email;

        private String date;
    }
}