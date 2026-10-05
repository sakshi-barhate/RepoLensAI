package com.repolens.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepositoryAnalysisRequest {

    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;

}