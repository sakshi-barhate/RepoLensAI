package com.repolens.backend.controller;

import com.repolens.backend.dto.request.RepositoryAnalysisRequest;
import com.repolens.backend.dto.response.RepositoryAnalysisResponse;
import com.repolens.backend.service.RepositoryAnalysisService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/repository")
public class RepositoryAnalysisController {

    @Autowired
    private RepositoryAnalysisService repositoryAnalysisService;

    @PostMapping("/analyze")
    public RepositoryAnalysisResponse analyzeRepository(
            @Valid @RequestBody RepositoryAnalysisRequest request) {

        try {
            return repositoryAnalysisService.analyzeRepository(request);

        } catch (Exception e) {

            e.printStackTrace();

            throw e;
        }
    }
}