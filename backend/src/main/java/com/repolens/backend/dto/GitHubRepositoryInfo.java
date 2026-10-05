package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GitHubRepositoryInfo {

    private String owner;

    private String repository;

    private String description;

    private String defaultBranch;

    private int stars;

    private int forks;

    private int watchers;

    private int openIssues;

    private String lastUpdated;

    private String homepage;

    private List<String> topics;
}