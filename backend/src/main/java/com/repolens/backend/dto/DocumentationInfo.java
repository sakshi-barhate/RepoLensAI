package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentationInfo {

    private int javaDocComments;
    private int markdownFiles;
    private boolean readmeExists;
    private String documentationScore;
}