package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DuplicationInfo {

    private int duplicateFiles;

    private List<String> duplicatedClasses;

    private String risk;
}