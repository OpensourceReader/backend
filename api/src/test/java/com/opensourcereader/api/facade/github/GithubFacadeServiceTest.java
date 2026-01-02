package com.opensourcereader.api.facade.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.opensourcereader.api.client.GithubClient;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.impl.LocalOpenSourceRepoService;
import com.opensourcereader.core.board.service.IssueSyncService;
import com.opensourcereader.core.user.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockRestServiceServer
@Transactional
@ActiveProfiles("h2")
public class GithubFacadeServiceTest {
  private static final FixtureMonkey FIXTURE_MONKEY =
      FixtureMonkey.builder()
          .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
          .pushExactTypeArbitraryIntrospector(
              GithubRepoRequest.class, ConstructorPropertiesArbitraryIntrospector.INSTANCE)
          .defaultNotNull(true)
          .build();

  @Autowired private GithubClient githubClient;

  @Autowired private GithubModelMapper modelMapper;

  @Autowired private LocalOpenSourceRepoService openSourceRepoService;

  @Autowired private UserServiceImpl userService;

  @Autowired private IssueSyncService issueSyncService;

  @Autowired private MockRestServiceServer mockServer;

  @Autowired private GitHubFacadeService gitHubFacadeService;

  @Test
  @DisplayName("레포지토리 JSON 데이터를 요청해서 성공적으로 받는다.")
  void createRepoTest() {
    // given
    GithubRepoRequest request = new GithubRepoRequest("OpensourceReader", "backend");

    String gitHubApiResponseJson =
        """
          {
            "id" : 1102936168,
            "name" : "%s",
            "owner" : {
                "id" : 245825325,
                "login" : "%s",
                "avatar_url" : "https://avatars.githubusercontent.com/u/245825325?v=4"
              },
            "created_at" : "2025-11-24T08:23:45Z",
            "updated_at" : "2025-12-11T08:43:04Z"
          }
        """
            .formatted(request.repoName(), request.owner());

    String url =
        "https://api.github.com/repos/%s/%s".formatted(request.owner(), request.repoName());

    mockServer
        .expect(requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(gitHubApiResponseJson, MediaType.APPLICATION_JSON));

    // when
    OpenSourceRepo repo = gitHubFacadeService.createRepo(request);
    // then
    assertThat(repo).isNotNull();
    assertThat(repo.getTitle()).isEqualTo(request.repoName());

    OpenSourceRepo savedRepo =
        openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(), request.repoName());
    assertThat(savedRepo).isNotNull();
  }
}
