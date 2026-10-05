package com.repolens.backend.service;

import com.repolens.backend.dto.github.GitHubPullRequestFileResponse;
import com.repolens.backend.dto.github.GitHubPullRequestResponse;
import com.repolens.backend.exception.GitHubApiException;
import com.repolens.backend.exception.PullRequestNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PullRequestService {

    private final RestTemplate restTemplate;

    public PullRequestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GitHubPullRequestResponse getPullRequest(
            String owner,
            String repository,
            int pullRequestNumber) {

        String url = "https://api.github.com/repos/"
                + owner + "/"
                + repository
                + "/pulls/"
                + pullRequestNumber;

        try {

            return restTemplate.getForObject(
                    url,
                    GitHubPullRequestResponse.class
            );

        } catch (HttpClientErrorException.NotFound e) {

            throw new PullRequestNotFoundException(
                    "Pull request not found: "
                            + owner + "/"
                            + repository + "#"
                            + pullRequestNumber
            );

        } catch (HttpClientErrorException.Forbidden e) {

            throw new GitHubApiException(
                    "GitHub API access denied or rate limit exceeded."
            );

        } catch (HttpClientErrorException.Unauthorized e) {

            throw new GitHubApiException(
                    "GitHub API authentication failed."
            );
        }
    }

    public List<GitHubPullRequestFileResponse> getPullRequestFiles(
            String owner,
            String repository,
            int pullRequestNumber) {

        String url = "https://api.github.com/repos/"
                + owner + "/"
                + repository
                + "/pulls/"
                + pullRequestNumber
                + "/files";

        try {

            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<
                            List<GitHubPullRequestFileResponse>>() {}
            ).getBody();

        } catch (HttpClientErrorException.NotFound e) {

            throw new PullRequestNotFoundException(
                    "Pull request files not found: "
                            + owner + "/"
                            + repository + "#"
                            + pullRequestNumber
            );

        } catch (HttpClientErrorException.Forbidden e) {

            throw new GitHubApiException(
                    "GitHub API access denied or rate limit exceeded."
            );

        } catch (HttpClientErrorException.Unauthorized e) {

            throw new GitHubApiException(
                    "GitHub API authentication failed."
            );
        }
    }
}