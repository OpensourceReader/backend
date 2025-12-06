package com.opensourcereader.api.fetcher.analysis;

import com.opensourcereader.api.dto.GitTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GitTreeFetcher {

  private final RestTemplate restTemplate;

  public GitTreeResponse fetchGitTree(String treeApiUrl) {
    return restTemplate.getForObject(treeApiUrl, GitTreeResponse.class);
  }

}
