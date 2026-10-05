package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PullRequestAnalysisResult {

    private int pullRequestNumber;

    private String title;

    private String author;

    private String state;

    private int changedFiles;

    private int additions;

    private int deletions;

    private int totalChanges;

    private List<String> securityIssues;

    private List<String> qualityIssues;

    private AiReview aiReview;
}