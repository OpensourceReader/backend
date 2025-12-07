package com.opensourcereader.core.analysis.service.basic;

import com.opensourcereader.core.analysis.dto.GitTreeFileDetail;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.ContentType;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@ConditionalOnProperty(
    value = "opensource.ingest-mode",
    havingValue = "github-api"
)
@Service
@RequiredArgsConstructor
public class RepoContentRawTextFetcher {

  private final RestTemplate restTemplate;

  public String fetchRepoContent(GitTreeFileInfo fileInfo) {
    if (fileInfo.type().equals(ContentType.TREE)) {
      return null;
    }
    GitTreeFileDetail fileDetail = restTemplate.getForObject(
        fileInfo.url(),
        GitTreeFileDetail.class
    );
    if (fileDetail == null || fileDetail.content() == null) {
      throw new IllegalArgumentException("file info url is invalid");
    }
    byte[] decodedBytes = Base64.getMimeDecoder().decode(fileDetail.content());
    return new String(decodedBytes);
  }

}
