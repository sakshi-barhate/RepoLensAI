package com.repolens.backend.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.repolens.backend.config.GeminiConfig;
import com.repolens.backend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeminiServiceImpl implements GeminiService {

    private static final String PRIMARY_MODEL = "gemini-3.8-flash";
    private static final String FALLBACK_MODEL = "gemini-3.7-flash";

    @Autowired
    private GeminiConfig geminiConfig;

    @Override
    public String generateRepositorySummary(String prompt) {
        try {
            Client client = Client.builder()
                    .apiKey(geminiConfig.getApiKey())
                    .build();

            GenerateContentResponse response =
                    generateWithRetryAndFallback(client, prompt);

            if (response != null
                    && response.text() != null
                    && !response.text().isBlank()) {
                return response.text().trim();
            }

            System.err.println("Gemini returned an empty response.");
            return "AI summary is currently unavailable.";

        } catch (Exception e) {
            String message = e.getMessage();
            System.err.println("Gemini API error: " + message);

            if (isQuotaError(message)) {
                return "AI summary temporarily unavailable because the Gemini API quota has been reached. Please try again later.";
            }

            if (isServiceUnavailable(message)) {
                return "AI summary is temporarily unavailable because Gemini is busy. Please try again shortly.";
            }

            return "AI summary is currently unavailable.";
        }
    }

    private GenerateContentResponse generateWithRetryAndFallback(
            Client client,
            String prompt) throws Exception {

        try {
            return client.models.generateContent(
                    PRIMARY_MODEL,
                    prompt,
                    null
            );
        } catch (Exception firstError) {
            if (!isServiceUnavailable(firstError.getMessage())) {
                throw firstError;
            }

            System.err.println(
                    PRIMARY_MODEL + " is busy; retrying once."
            );
            pauseBeforeRetry();

            try {
                return client.models.generateContent(
                        PRIMARY_MODEL,
                        prompt,
                        null
                );
            } catch (Exception retryError) {
                if (!isServiceUnavailable(retryError.getMessage())) {
                    throw retryError;
                }

                System.err.println(
                        PRIMARY_MODEL + " is still busy; trying "
                                + FALLBACK_MODEL + "."
                );
                return client.models.generateContent(
                        FALLBACK_MODEL,
                        prompt,
                        null
                );
            }
        }
    }

    private void pauseBeforeRetry() throws InterruptedException {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private boolean isServiceUnavailable(String message) {
        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("503")
                || normalized.contains("high demand")
                || normalized.contains("service unavailable")
                || normalized.contains("unavailable");
    }

    private boolean isQuotaError(String message) {
        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("429")
                || normalized.contains("resource_exhausted");
    }
}