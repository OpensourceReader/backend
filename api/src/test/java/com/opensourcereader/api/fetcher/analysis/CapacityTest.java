package com.opensourcereader.api.fetcher.analysis;

import com.opensourcereader.api.IntegrationTestSupport;
import com.opensourcereader.api.dto.GitTreeResponse;
import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpensourceRepo;
import com.opensourcereader.core.analysis.service.OpensourceRepoService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CapacityTest extends IntegrationTestSupport {

  @Autowired
  OpensourceRepoService opensourceRepoService;

  @Autowired
  GitTreeFetcher gitTreeFetcher;

  @Disabled
  @DisplayName("fetch 임시 테스트")
  @Test
  void fetchService() {
    // given
    String treeApiUrl = "https://api.github.com/repos/OpensourceReader/backend/git/trees/main?recursive=1";

    // when
    GitTreeResponse response = gitTreeFetcher.fetchGitTree(treeApiUrl);
    GitTree gitTree = new GitTree(treeApiUrl, response.flatTrees());

    OpensourceRepo opensourceRepo = opensourceRepoService.create(gitTree);

    // then
    System.out.println("opensourceRepo: " + opensourceRepo);
  }

}
