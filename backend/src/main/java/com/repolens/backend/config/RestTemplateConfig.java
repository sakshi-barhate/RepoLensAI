package com.repolens.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(
            @Value("${GITHUB_TOKEN:}") String githubToken) {

        RestTemplate restTemplate = new RestTemplate();

        if (githubToken != null && !githubToken.isBlank()) {
            restTemplate.getInterceptors().add((request, body, execution) -> {
                if ("api.github.com".equalsIgnoreCase(
                        request.getURI().getHost())) {
                    request.getHeaders().setBearerAuth(githubToken.trim());
                    request.getHeaders().set(
                            "X-GitHub-Api-Version",
                            "2022-11-28"
                    );
                }

                return execution.execute(request, body);
            });
        }

        return restTemplate;
    }
}