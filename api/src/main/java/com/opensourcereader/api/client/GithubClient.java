package com.opensourcereader.api.client;

import com.opensourcereader.api.client.response.GithubRepoResponse;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.api.client.response.GithubPullResponse;
import com.opensourcereader.api.controller.auth.response.GitHubApiEmailResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GithubClient {

  @Value("${github.token}")
  private String token;

  private final RestClient restClient;

  public GithubRepoResponse fetchRepo(GithubRepoRequest request) {
    ResponseEntity<GithubRepoResponse> response = restClient.get()
        .uri("/repos/{owner}/{repoName}", request.owner(), request.repoName())
        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
        .retrieve()
        .toEntity(GithubRepoResponse.class);
    return response.getBody();
  }

  // /repos/주인/레포이름/issues
  public List<GithubIssueResponse> fetchRepoIssues(GithubRepoRequest request) {
    ResponseEntity<List<GithubIssueResponse>> response =
        restClient
            .get()
            .uri("/repos/{owner}/{repoName}/issues", request.owner(), request.repoName())
            .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<GithubIssueResponse>>() {});
    return response.getBody();
  }

  // /repos/주인/레포이름/pulls
  public List<GithubPullResponse> fetchRepoPulls(GithubRepoRequest request) {
    ResponseEntity<List<GithubPullResponse>> response =
        restClient
            .get()
            .uri("/repos/{owner}/{repoName}/pulls", request.owner(), request.repoName())
            .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<GithubPullResponse>>() {});
    return response.getBody();
  }

  public List<GitHubApiEmailResponse> fetchUserEmails(String accessToken) {
    ResponseEntity<List<GitHubApiEmailResponse>> response =
        restClient
            .get()
            .uri("/user/emails")
            .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<GitHubApiEmailResponse>>() {});

    return response.getBody();
  }
}
