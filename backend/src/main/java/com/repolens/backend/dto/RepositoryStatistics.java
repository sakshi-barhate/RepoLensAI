package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepositoryStatistics {

    private int totalFiles;
    private int totalDirectories;
}