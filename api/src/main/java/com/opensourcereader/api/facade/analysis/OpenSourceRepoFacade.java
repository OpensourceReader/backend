package com.opensourcereader.api.facade.analysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoResponse;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.util.FileUtil;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenSourceRepoFacade {

  @Value("${opensource-reader.local-clone-path}")
  private String localClonePath;

  private final GitRepositoryService gitRepositoryService;
  private final OpenSourceRepoService opensourceRepoService;

  @Transactional
  public OpenSourceRepoResponse createRepo(OpenSourceRepoCreateRequest request) {
    String savedLocalPath =
        gitRepositoryService.saveToLocal(request.openSourceUri(), localClonePath);
    OpenSourceRepo openSourceRepo =
        opensourceRepoService.createRepo(
            savedLocalPath, request.openSourceUri(), request.repoReference());
    FileUtil.removeDirectory(savedLocalPath);

    return OpenSourceRepoResponse.from(openSourceRepo);
  }

  @Transactional
  public OpenSourceRepoResponse getRepoById(Long repoId) {
    OpenSourceRepo openSourceRepo = opensourceRepoService.getRepoById(repoId);
    return OpenSourceRepoResponse.from(openSourceRepo);
  }

  @Transactional
  public void deleteRepoById(Long repoId) {
    opensourceRepoService.deleteRepoById(repoId);
  }
}
