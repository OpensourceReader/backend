package com.opensourcereader.core.analysis.service.basic;

import static org.assertj.core.api.Assertions.assertThat;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

  @Disabled
  @DisplayName("fetcherRawText")
  @Test
  void fetcherRawTextBlob() {
    // given
    String fileInfoBlobUrl = "https://api.github.com/repos/hibernate/hibernate-orm/git/blobs/5a56b1c3d9e795f0cf566991d1060d900c1c7161";
    GitTreeFileInfo fileInfo = new GitTreeFileInfo(null, ContentType.SOURCE_CODE, fileInfoBlobUrl,
        null);

    // when
    String rawText = repoContentRawTextFetcher.fetchRepoContent(fileInfo);

    // then
    assertThat(rawText).isNotNull();
  }

  @Disabled
  @DisplayName("fetcherRawText")
  @Test
  void fetcherRawTextTree() {
    // given
    String fileInfoTreeUrl = "https://api.github.com/repos/hibernate/hibernate-orm/git/trees/b33c2e7d3e493fb806ea6ba26557e3b925351559";
    GitTreeFileInfo fileInfo = new GitTreeFileInfo(null, ContentType.TREE, fileInfoTreeUrl,
        null);

    // when
    String rawText = repoContentRawTextFetcher.fetchRepoContent(fileInfo);

    // when & then
    assertThat(rawText).isNull();
  }

}