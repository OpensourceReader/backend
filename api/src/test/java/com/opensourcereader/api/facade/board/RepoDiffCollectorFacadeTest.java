package com.opensourcereader.api.facade.board;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import com.opensourcereader.api.client.GithubClient;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.board.service.IssueRetrieveService;
import com.opensourcereader.core.board.service.PullRequestFileService;
import com.opensourcereader.core.user.dto.UserSignUpCommand;
import com.opensourcereader.core.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepoDiffCollectorFacadeTest {

  @Mock private GithubClient githubClient;

  @Mock private OpenSourceRepoService openSourceRepoService;

  @Mock private IssueRetrieveService issueRetrieveService;

  @Mock private PullRequestFileService pullRequestFileService;

  @InjectMocks private RepoDiffCollectorFacade repoDiffCollectorFacade;

  @Test
  void success_collectAndSaveDiffs_test() {
    // given
    GithubRepoRequest request = createRepoRequest();
    OpenSourceRepo repo = createMockRepo(request);

    Queue<Integer> mockTagNumbers = new ArrayDeque<>(List.of(1, 2));

    when(openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(), request.repoName()))
        .thenReturn(repo);
    when(issueRetrieveService.findPullByRepository(repo)).thenReturn(mockTagNumbers);
    when(githubClient.fetchDiff(any())).thenReturn("diff content");
    when(pullRequestFileService.save(anyMap())).thenReturn(true);
    // when
    boolean result = repoDiffCollectorFacade.collectAndSaveDiffs(request);
    // then
    assertAll(
        () -> assertTrue(result),
        () -> verify(githubClient, times(2)).fetchDiff(any()),
        () -> verify(pullRequestFileService).save(anyMap()));
  }

  private GithubRepoRequest createRepoRequest() {
    return new GithubRepoRequest("owner", "repoName");
  }

  private OpenSourceRepo createMockRepo(GithubRepoRequest request) {
    User user =
        User.from(new UserSignUpCommand("aaa@bbb.com", "abcde", "test", "test123", "userName"));
    return new OpenSourceRepo(user, request.repoName());
  }
}
