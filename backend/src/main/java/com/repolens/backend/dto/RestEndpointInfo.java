package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestEndpointInfo {

    private String method;

    private String path;
}