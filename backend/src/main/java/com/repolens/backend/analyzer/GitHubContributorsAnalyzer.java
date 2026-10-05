package com.repolens.backend.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repolens.backend.dto.ContributorInfo;
import com.repolens.backend.dto.ContributorsInfo;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitHubContributorsAnalyzer {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public ContributorsInfo analyze(String repositoryUrl) {

        try {

            String repo = repositoryUrl
                    .replace("https://github.com/", "")
                    .replace(".git", "");

            String api =
                    "https://api.github.com/repos/" +
                            repo +
                            "/contributors";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(api))
                    .header("Accept", "application/vnd.github+json")
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode array = mapper.readTree(response.body());

            List<ContributorInfo> contributors = new ArrayList<>();

            for (JsonNode node : array) {

                contributors.add(
                        ContributorInfo.builder()
                                .name(node.path("login").asText())
                                .contributions(node.path("contributions").asInt())
                                .build()
                );
            }

            return ContributorsInfo.builder()
                    .totalContributors(contributors.size())
                    .contributors(contributors)
                    .build();

        } catch (Exception e) {

            return ContributorsInfo.builder()
                    .totalContributors(0)
                    .contributors(new ArrayList<>())
                    .build();
        }
    }
}