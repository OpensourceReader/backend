package com.opensourcereader.api.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.opensourcereader.api.client.request.GithubIssueCommentRequest;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.api.client.response.GithubIssueCommentResponse;
import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.api.client.response.GithubPullResponse;
import com.opensourcereader.api.client.response.GithubRepoResponse;
import com.opensourcereader.api.client.response.GithubReviewResponse;
import com.opensourcereader.api.controller.auth.response.GitHubApiEmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubClient {

  private static final int MAX_RETRY_LIMIT = 3;
  private static final long RETRY_DELAY_MS = 500L;

  @Value("${github.token}")
  private String token;

  private final RestClient restClient;

  // TODO 실패 태그 번호 한꺼번에 보내는 로직 만들어야함
  List<String> failedTags = new ArrayList<>();

  public GithubRepoResponse fetchRepo(GithubRepoRequest request) {
    ResponseEntity<GithubRepoResponse> response =
        restClient
            .get()
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
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/repos/{owner}/{repoName}/issues")
                        .queryParam("state", "all")
                        .queryParam("per_page", 100)
                        .build(request.owner(), request.repoName()))
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

  public List<GithubPullResponse> fetchRepoPulls(
      GithubRepoRequest request, Queue<Integer> tagNumbers) {
    List<GithubPullResponse> response = new ArrayList<>();
    Map<Integer, Integer> retryCounts = new HashMap<>();

    while (!tagNumbers.isEmpty()) {
      Integer tagNumber = tagNumbers.poll();

      try {
        GithubPullResponse body =
            restClient
                .get()
                .uri(
                    "/repos/{owner}/{repoName}/pulls/{tagNumber}",
                    request.owner(),
                    request.repoName(),
                    tagNumber)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .body(GithubPullResponse.class);
        response.add(body);
        retryCounts.remove(tagNumber);

      } catch (RestClientException e) {
        int currentRetry = retryCounts.getOrDefault(tagNumber, 0);

        if (currentRetry < MAX_RETRY_LIMIT) {
          retryCounts.put(tagNumber, currentRetry + 1);
          tagNumbers.offer(tagNumber);

          log.warn("이슈 재시도 예약(Tag: {}, Count: {} )", tagNumber, currentRetry + 1);

          // 서버 보호를 위한 지연 시간
          try {
            TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }
        } else {
          log.error("이슈 최대 재시도 초과, 건너뜀 (Tag: {}): {}", tagNumber, e.getMessage());
          failedTags.add("I" + request.owner() + request.repoName() + tagNumber);
        }
      }
    }
    return response;
  }

  public List<GithubIssueCommentResponse> fetchRepoIssueComments(
      GithubIssueCommentRequest request) {
    return restClient
        .get()
        .uri(
            "/repos/{owner}/{repoName}/issues/{tagNumber}/comments",
            request.owner(),
            request.repoName(),
            request.tagNumber())
        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
        .retrieve()
        .toEntity(new ParameterizedTypeReference<List<GithubIssueCommentResponse>>() {})
        .getBody();
  }

  public Map<Integer, List<GithubIssueCommentResponse>> fetchRepoIssueComments(
      Queue<GithubIssueCommentRequest> requests) {
    Map<Integer, List<GithubIssueCommentResponse>> response = new HashMap<>();
    Map<Integer, Integer> retryCounts = new HashMap<>();

    while (!requests.isEmpty()) {
      GithubIssueCommentRequest request = requests.poll();
      Integer tagNumber = request.tagNumber();

      try {
        List<GithubIssueCommentResponse> body =
            restClient
                .get()
                .uri(
                    "/repos/{owner}/{repoName}/issues/{tagNumber}/comments",
                    request.owner(),
                    request.repoName(),
                    tagNumber)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<GithubIssueCommentResponse>>() {})
                .getBody();

        response.put(tagNumber, body);
        retryCounts.remove(tagNumber);
      } catch (RestClientException e) {
        int currentRetry = retryCounts.getOrDefault(tagNumber, 0);
        if (currentRetry < MAX_RETRY_LIMIT) {
          retryCounts.put(tagNumber, currentRetry + 1);
          requests.add(request);

          log.warn("이슈 코멘트 재시도 예약(Tag: {}, Count: {} )", tagNumber, currentRetry + 1);

          // 서버 보호를 위한 지연 시간
          try {
            TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }

        } else {
          log.error("이슈 코멘트 최대 재시도 초과, 건너뜀 (Tag: {}): {}", tagNumber, e.getMessage());
          failedTags.add("IC" + request.owner() + request.repoName() + tagNumber);
        }
      }
    }

    return response;
  }

  public List<GithubReviewResponse> fetchRepoReviews(GithubIssueCommentRequest request) {
    return restClient
        .get()
        .uri(
            "/repos/{owner}/{repoName}/pulls/{tagNumber}/reviews",
            request.owner(),
            request.repoName(),
            request.tagNumber())
        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
        .retrieve()
        .toEntity(new ParameterizedTypeReference<List<GithubReviewResponse>>() {})
        .getBody();
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
