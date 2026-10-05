package com.repolens.backend.service;

import com.repolens.backend.dto.request.RepositoryAnalysisRequest;
import com.repolens.backend.dto.response.RepositoryAnalysisResponse;

public interface RepositoryAnalysisService {

    RepositoryAnalysisResponse analyzeRepository(RepositoryAnalysisRequest request);

}