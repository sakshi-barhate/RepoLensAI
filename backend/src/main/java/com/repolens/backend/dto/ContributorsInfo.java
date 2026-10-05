package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContributorsInfo {

    private int totalContributors;

    private List<ContributorInfo> contributors;
}