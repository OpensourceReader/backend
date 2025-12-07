package com.opensourcereader.api.fetcher.analysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.opensourcereader.api.dto.GitTreeResponse;
import com.opensourcereader.core.config.GitHubRestTemplateConfig;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ActiveProfiles("test")
@RestClientTest(GitTreeFetcher.class)
@Import(GitHubRestTemplateConfig.class)
class GitTreeFetcherTest {

  @Autowired
  GitTreeFetcher gitTreeFetcher;

  @Disabled
  @DisplayName("fetchTest")
  @Test
  void fetchTest() {
    // given
    String treeApiUrl =
        "https://api.github.com/repos/hibernate/hibernate-orm/git/trees/main?recursive=1";

    // when
    GitTreeResponse response = gitTreeFetcher.fetchGitTree(treeApiUrl);

    // then
    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(response).isNotNull();
          softly.assertThat(response.flatTrees()).isNotNull();
        });
  }
}
