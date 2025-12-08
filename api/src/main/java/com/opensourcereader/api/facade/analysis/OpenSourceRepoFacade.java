package com.opensourcereader.api.facade.analysis;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoCreateResponse;
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

  private final static String LOCAL_DIRECTORY = "/clone-local-repo/";

  private final GitRepositoryService localGitRepositoryService;
  private final OpenSourceRepoService opensourceRepoService;

  @Transactional
  public OpenSourceRepoCreateResponse create(OpenSourceRepoCreateRequest request) {
    String savedLocalPath =
        localGitRepositoryService.saveToLocal(request.openSourceUri(), LOCAL_DIRECTORY);
    GitTree treeOfRepo =
        localGitRepositoryService.getFlatTree(savedLocalPath, request.repoReference());
    OpenSourceRepo opensourceRepo = opensourceRepoService.create(treeOfRepo);

    return OpenSourceRepoCreateResponse.from(opensourceRepo);
  }
}
