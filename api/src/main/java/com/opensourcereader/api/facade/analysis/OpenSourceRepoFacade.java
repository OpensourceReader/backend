package com.opensourcereader.api.facade.analysis;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoResponse;
import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenSourceRepoFacade {

  private final static String LOCAL_DIRECTORY = "local-clone-repo";

  private final GitRepositoryService gitRepositoryService;
  private final OpenSourceRepoService opensourceRepoService;

  @Transactional
  public OpenSourceRepoResponse createRepo(OpenSourceRepoCreateRequest request) {
    String savedLocalPath =
        gitRepositoryService.saveToLocal(request.openSourceUri(), LOCAL_DIRECTORY);
    GitTree treeOfRepo =
        gitRepositoryService.getFlatTree(savedLocalPath, request.repoReference());
    OpenSourceRepo openSourceRepo = opensourceRepoService.createRepo(treeOfRepo);

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
