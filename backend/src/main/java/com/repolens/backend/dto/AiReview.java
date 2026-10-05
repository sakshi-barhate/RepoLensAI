package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiReview {

    private String summary;

    private String security;

    private String codeQuality;

    private String recommendations;

    private String overallReview;
}