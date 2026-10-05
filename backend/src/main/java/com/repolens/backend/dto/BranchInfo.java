package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BranchInfo {

    private int totalBranches;

    private String defaultBranch;
}