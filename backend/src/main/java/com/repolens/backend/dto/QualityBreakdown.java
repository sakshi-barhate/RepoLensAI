package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QualityBreakdown {

    private int readme;
    private int license;
    private int tests;
    private int githubActions;
    private int docker;
    private int dockerCompose;
    private int projectStructure;
    private int security;
    private int documentation;
    private int codeQuality;
}