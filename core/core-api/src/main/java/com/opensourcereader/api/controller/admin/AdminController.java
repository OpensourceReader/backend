package com.opensourcereader.api.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.client.request.GithubIssueCommentRequest;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.api.facade.github.GitHubFacadeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
  // TODO 주의!!! 해당 api는 배포할 때는 닫아두거나 삭제해야함. 테스트 목적임

  private final GitHubFacadeService gitHubFacadeService;

  @PostMapping("/all")
  public ResponseEntity<Boolean> fetchAll(@RequestBody GithubRepoRequest request) {
    gitHubFacadeService.createIssues(request);
    gitHubFacadeService.createPulls(request);
    gitHubFacadeService.createIssueComments(request);

    return ResponseEntity.ok(true);
  }

  @PostMapping("/all/comments")
  public ResponseEntity<Boolean> fetchAllComments(@RequestBody GithubIssueCommentRequest request) {
    gitHubFacadeService.createReviews(request);
    gitHubFacadeService.createPullComments(request);

    return ResponseEntity.ok(true);
  }

  @PostMapping("/issues")
  public ResponseEntity<Boolean> fetchIssues(@RequestBody GithubRepoRequest request) {
    boolean success = gitHubFacadeService.createIssues(request);

    return ResponseEntity.ok(success);
  }

  @PostMapping("/pulls")
  public ResponseEntity<Boolean> fetchPulls(@RequestBody GithubRepoRequest request) {
    boolean success = gitHubFacadeService.createPulls(request);

    return ResponseEntity.ok(success);
  }

  @PostMapping("/reviews")
  public ResponseEntity<Boolean> fetchReviews(@RequestBody GithubIssueCommentRequest request) {
    boolean success = gitHubFacadeService.createReviews(request);

    return ResponseEntity.ok(success);
  }

  @PostMapping("/comments")
  public ResponseEntity<Boolean> fetchComments(@RequestBody GithubRepoRequest request) {
    boolean success = gitHubFacadeService.createIssueComments(request);

    return ResponseEntity.ok(success);
  }

  @PostMapping("/pulls/comments")
  public ResponseEntity<Boolean> fetchPullComments(@RequestBody GithubIssueCommentRequest request) {
    boolean success = gitHubFacadeService.createPullComments(request);

    return ResponseEntity.ok(success);
  }
}
