package com.opensourcereader.core.analysis.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RepoContentRawTextFetcherTest {

  RestTemplate restTemplate;
  RepoContentRawTextFetcher repoContentRawTextFetcher;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    repoContentRawTextFetcher = new RepoContentRawTextFetcher(
        restTemplate);
  }


  @DisplayName("fetcherRawText")
  @Test
  void fetcherRawTextBlob() {
    // given
    String fileInfoBlobUrl = "https://api.github.com/repos/hibernate/hibernate-orm/git/blobs/5a56b1c3d9e795f0cf566991d1060d900c1c7161";

    // when
    String rawText = repoContentRawTextFetcher.fetchRepoContent(fileInfoBlobUrl);

    // then
    assertThat(rawText).isNotNull();
  }


  @DisplayName("fetcherRawText")
  @Test
  void fetcherRawTextTree() {
    // given
    String fileInfoTreeUrl = "https://api.github.com/repos/hibernate/hibernate-orm/git/trees/b33c2e7d3e493fb806ea6ba26557e3b925351559";

    // when & then
    assertThatThrownBy(() -> repoContentRawTextFetcher.fetchRepoContent(fileInfoTreeUrl))
        .isInstanceOf(IllegalArgumentException.class);
  }
  
}