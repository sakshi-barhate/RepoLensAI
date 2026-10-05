package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodeMetrics {

    private int classes;

    private int interfaces;

    private int enums;

    private int records;

    private int packages;
}