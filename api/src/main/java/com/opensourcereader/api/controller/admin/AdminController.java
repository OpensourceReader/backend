package com.opensourcereader.api.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.api.facade.github.GitHubFacadeService;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
  // TODO 주의!!! 해당 api는 배포할 때는 닫아두거나 삭제해야함. 테스트 목적임

  private final GitHubFacadeService gitHubFacadeService;

  @PostMapping("/repo")
  public ResponseEntity<OpenSourceRepo> fetchRepo(@RequestBody GithubRepoRequest request) {
    OpenSourceRepo repo = gitHubFacadeService.createRepo(request);

    return ResponseEntity.ok(repo);
  }

  @PostMapping("/issues")
  public ResponseEntity<List<Issue>> fetchIssues(@RequestBody GithubRepoRequest request) {
    List<Issue> issues = gitHubFacadeService.createIssues(request);

    return ResponseEntity.ok(issues);
  }

  @PostMapping("/pulls")
  public ResponseEntity<List<Pull>> fetchPulls(@RequestBody GithubRepoRequest request) {
    List<Pull> pulls = gitHubFacadeService.createPulls(request);

    return ResponseEntity.ok(pulls);
  }
}
