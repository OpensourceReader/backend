package com.opensourcereader.api.facade.board;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.client.GithubClient;
import com.opensourcereader.api.client.request.GithubIssueCommentRequest;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.board.service.IssueRetrieveService;
import com.opensourcereader.core.board.service.PullRequestFileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepoDiffCollectorFacade {

  private final GithubClient githubClient;

  private final OpenSourceRepoService openSourceRepoService;
  private final IssueRetrieveService issueRetrieveService;
  private final PullRequestFileService pullRequestFileService;

  public boolean collectAndSaveDiffs(GithubRepoRequest request) {
    OpenSourceRepo repo =
        openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(), request.repoName());
    Queue<Integer> pullTagNumbers = issueRetrieveService.findPullByRepository(repo);
    Map<Integer, String> diffMap = new HashMap<>();

    while (!pullTagNumbers.isEmpty()) {
      Integer tagNumber = pullTagNumbers.poll();
      String diff =
          githubClient.fetchDiff(
              new GithubIssueCommentRequest(request.owner(), request.repoName(), tagNumber));
      diffMap.put(tagNumber, diff);
    }
    return pullRequestFileService.save(diffMap);
  }
}
