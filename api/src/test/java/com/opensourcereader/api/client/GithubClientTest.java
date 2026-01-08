package com.opensourcereader.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import com.opensourcereader.api.client.request.GithubIssueCommentRequest;
import com.opensourcereader.api.config.HttpConfig;
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
    GithubIssueCommentRequest request = new GithubIssueCommentRequest("owner", "repoName", 1);
    String expectedUri = "https://github.com/owner/repoName/pull/1.diff";
    String expectedResponse = "{\"status\":\"success\"}";

    this.server
        .expect(requestTo(expectedUri))
        .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

    // when
    String result = githubClient.fetchDiff(request);

    // then
    assertThat(result).contains("success");
    this.server.verify();
  }
}
