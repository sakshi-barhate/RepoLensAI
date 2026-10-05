package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodeQualityInfo {

    // Overall maintainability
    private String maintainability;

    // Code quality metrics
    private int codeSmells;
    private String technicalDebt;
    private String rating;

    // Comment metrics
    private int todoComments;
    private int fixmeComments;
    private int commentLines;

    // General metrics
    private int blankLines;
    private int codeLines;
}