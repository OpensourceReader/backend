package com.opensourcereader.api.fetcher.analysis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.opensourcereader.api.dto.GitTreeResponse;

import lombok.RequiredArgsConstructor;

@ConditionalOnProperty(
    value = "opensource.ingest-mode",
    havingValue = "github-api"
)
@Service
@RequiredArgsConstructor
public class GitTreeFetcher {

  private final RestTemplate restTemplate;

  public GitTreeResponse fetchGitTree(String treeApiUrl) {
    return restTemplate.getForObject(treeApiUrl, GitTreeResponse.class);
  }
}
