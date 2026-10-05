package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RestApiInfo {

    private int getEndpoints;

    private int postEndpoints;

    private int putEndpoints;

    private int deleteEndpoints;

    private int patchEndpoints;

    private List<RestEndpointInfo> endpoints;
}