package com.opensourcereader.api.facade.analysis;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoCreateResponse;
import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.analysis.service.basic.LocalGitRepositoryService;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenSourceRepoFacade {

  private final LocalGitRepositoryService localGitRepositoryService;
  private final OpenSourceRepoService opensourceRepoService;

  @Transactional
  public OpenSourceRepoCreateResponse create(OpenSourceRepoCreateRequest request) {
    String savedLocalPath =
        localGitRepositoryService.saveToLocal(request.openSourceUri(), request.localPath());
    GitTree treeOfRepo =
        localGitRepositoryService.getFlatTree(savedLocalPath, request.repoReference());
    OpenSourceRepo opensourceRepo = opensourceRepoService.create(treeOfRepo);

    return OpenSourceRepoCreateResponse.from(opensourceRepo);
  }
}
