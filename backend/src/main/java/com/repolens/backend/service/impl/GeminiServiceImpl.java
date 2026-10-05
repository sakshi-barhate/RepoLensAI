package com.repolens.backend.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.repolens.backend.config.GeminiConfig;
import com.repolens.backend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeminiServiceImpl implements GeminiService {

    @Autowired
    private GeminiConfig geminiConfig;

    @Override
    public String generateRepositorySummary(String prompt) {
        try {
            Client client = Client.builder()
                    .apiKey(geminiConfig.getApiKey())
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3.8-flash",
                    prompt,
                    null
            );

            if (response != null
                    && response.text() != null
                    && !response.text().isBlank()) {
                return response.text().trim();
            }

            System.err.println("Gemini returned an empty response.");
            return "AI summary is currently unavailable.";

        } catch (Exception e) {
            String message = e.getMessage();

            if (message != null && message.contains("429")) {
                return "AI summary temporarily unavailable because the Gemini API quota has been reached. Please try again later.";
            }

            System.err.println("Gemini API error: " + message);
            return "AI summary is currently unavailable.";
        }
    }
}