package com.opensourcereader.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import com.opensourcereader.api.client.request.GithubIssueCommentRequest;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.api.config.HttpConfig;
import com.opensourcereader.core.board.entity.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@RestClientTest(GithubClient.class)
@Import(HttpConfig.class)
public class GithubClientTest {

  @Autowired private GithubClient githubClient;

  @Autowired private MockRestServiceServer server;

  @Test
  @DisplayName("diff 문서를 성공적으로 호출한다.")
  void fetchDiffTest() {
    // given
    GithubIssueCommentRequest request = createMockIssueCommentRequest();
    String expectedUri = "https://github.com/owner/repoName/pull/1.diff";
    String expectedResponse = "{\"state\":\"success\"}";

    this.server
        .expect(requestTo(expectedUri))
        .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

    // when
    String result = githubClient.fetchDiff(request);

    // then
    assertThat(result).contains("success");
    this.server.verify();
  }

  @Test
  @DisplayName("state 문서를 성공적으로 호출한다.")
  void fetchStatusTest() {
    // given
    GithubRepoRequest request = createMockRepo();
    String expectedUri =
        "https://api.github.com/repos/owner/repoName/issues?state=all&per_page=100";
    String expectedResponse =
        """
        [
          {
            "id": 1,
            "number": 2,
            "state": "open",
            "title": "test Title"
          }
        ]
        """;

    this.server
        .expect(requestTo(expectedUri))
        .andRespond(
            withSuccess(expectedResponse, MediaType.valueOf("application/vnd.github+json")));
    // when
    List<GithubIssueResponse> result = githubClient.fetchRepoIssues(request);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).state()).isEqualTo(State.OPEN);
    assertThat(result.get(0).state().getValue()).isEqualTo("open");
  }

  private GithubRepoRequest createMockRepo() {
    return new GithubRepoRequest("owner", "repoName");
  }

  private GithubIssueCommentRequest createMockIssueCommentRequest() {
    return new GithubIssueCommentRequest("owner", "repoName", 1);
  }
}
