package com.repolens.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyzeRepositoryRequest {

    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;
}