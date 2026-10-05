package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContributorInfo {

    private String name;

    private int contributions;
}