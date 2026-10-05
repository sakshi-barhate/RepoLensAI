package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RepositorySizeInfo {

    private double totalSizeMB;

    private double sourceCodeMB;

    private double resourcesMB;

    private double testFilesMB;

    private double averageFileKB;

    private Map<String, Double> fileTypeSizes;
}