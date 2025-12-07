package com.opensourcereader.api.facade.analysis;

import com.opensourcereader.api.dto.OpensourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpensourceRepoCreateResponse;
import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpensourceRepo;
import com.opensourcereader.core.analysis.service.OpensourceRepoService;
import com.opensourcereader.core.analysis.service.basic.LocalGitRepoService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpensourceRepoFacade {

  private final LocalGitRepoService localGitRepoService;
  private final OpensourceRepoService opensourceRepoService;

  public OpensourceRepoCreateResponse create(OpensourceRepoCreateRequest request) {
    Repository repo = localGitRepoService.saveLocalToDirectory(
        request.opensourceUri(),
        request.localPath()
    );
    GitTree treeOfRepo = localGitRepoService.getFlatTreeOfRepo(repo, request.repoReference());
    OpensourceRepo opensourceRepo = opensourceRepoService.create(treeOfRepo);

    return OpensourceRepoCreateResponse.from(opensourceRepo);
  }

}
