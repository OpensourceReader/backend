package com.opensourcereader.api.facade.analysis;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoCreateResponse;
import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.analysis.service.basic.LocalGitRepoService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenSourceRepoFacade {

  private final LocalGitRepoService localGitRepoService;
  private final OpenSourceRepoService opensourceRepoService;

  public OpenSourceRepoCreateResponse create(OpenSourceRepoCreateRequest request) {
    Repository repo = localGitRepoService.saveLocalToDirectory(
        request.openSourceUri(),
        request.localPath()
    );
    GitTree treeOfRepo = localGitRepoService.getFlatTreeOfRepo(repo, request.repoReference());
    OpenSourceRepo opensourceRepo = opensourceRepoService.create(treeOfRepo);

    return OpenSourceRepoCreateResponse.from(opensourceRepo);
  }

}
