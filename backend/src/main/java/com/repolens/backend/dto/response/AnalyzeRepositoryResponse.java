package com.repolens.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AnalyzeRepositoryResponse {

    private String repositoryName;
    private String owner;
    private String language;
    private String framework;
    private Integer stars;
    private Integer forks;
    private String message;
}