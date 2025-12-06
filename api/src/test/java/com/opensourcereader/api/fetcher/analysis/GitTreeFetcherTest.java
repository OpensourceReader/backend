package com.opensourcereader.api.fetcher.analysis;

import com.opensourcereader.api.config.RestTemplateConfig;
import com.opensourcereader.api.dto.GitTreeResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

@RestClientTest({GitTreeFetcher.class, RestTemplateConfig.class})
class GitTreeFetcherTest {

  @Autowired
  GitTreeFetcher gitTreeFetcher;

  @DisplayName("fetchTest")
  @Test
  void fetchTest() {
    // given
    String treeApiUrl = "https://api.github.com/repos/hibernate/hibernate-orm/git/trees/main?recursive=1";

    // when
    GitTreeResponse response = gitTreeFetcher.fetchGitTree(treeApiUrl);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(response).isNotNull();
      softly.assertThat(response.flatTrees()).isNotNull();
    });
  }

}