package com.repolens.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplexityInfo {

    private int sourceFiles;

    private int linesOfCode;

    private int averageLinesPerFile;

    private String largestFile;

    private int largestFileLines;

    private String complexity;
}