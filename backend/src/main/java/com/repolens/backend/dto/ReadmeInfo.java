package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReadmeInfo {

    private String title;

    private String description;

    private List<String> technologies;
}