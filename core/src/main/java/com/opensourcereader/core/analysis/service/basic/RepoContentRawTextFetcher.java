package com.opensourcereader.core.analysis.service.basic;

import com.opensourcereader.core.analysis.dto.GitTreeFileDetail;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RepoContentRawTextFetcher {

  private final RestTemplate restTemplate;

  public String fetchRepoContent(String fileInfoUrl) {
    GitTreeFileDetail fileDetail = restTemplate.getForObject(fileInfoUrl, GitTreeFileDetail.class);
    if (fileDetail == null || fileDetail.content() == null) {
      throw new IllegalArgumentException("file info url is invalid");
    }
    byte[] decodedBytes = Base64.getMimeDecoder().decode(fileDetail.content());
    return new String(decodedBytes);
  }

}
