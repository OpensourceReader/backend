package com.opensourcereader.api.facade.board;

import com.opensourcereader.api.client.GithubClient;
import com.opensourcereader.api.client.request.GithubIssueCommentRequest;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.board.service.IssueRetrieveService;
import com.opensourcereader.core.board.service.PullCodeService;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodeConnectFacadeService {

  private final GithubClient githubClient;

  private final OpenSourceRepoService openSourceRepoService;
  private final IssueRetrieveService issueRetrieveService;
  private final PullCodeService pullCodeService;

  public boolean save(GithubRepoRequest request) {
    OpenSourceRepo repo = openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(),
        request.repoName());
    Queue<Integer> pullTagNumbers = issueRetrieveService.findPullByRepository(repo);
    Map<Integer, String> diffMap = new HashMap<>();

    while (!pullTagNumbers.isEmpty()) {
      Integer tagNumber = pullTagNumbers.poll();
      String diff = githubClient.fetchDiff(
          new GithubIssueCommentRequest(request.owner(), request.repoName(), tagNumber));
      diffMap.put(tagNumber, diff);
    }
    return pullCodeService.save(diffMap);
  }
}
