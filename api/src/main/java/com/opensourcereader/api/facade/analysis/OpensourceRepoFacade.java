package com.opensourcereader.api.facade.analysis;

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

  // response로 수정바람
  public OpensourceRepo create(String opensourceUri, String localPath, String repoReference) {
    Repository repo = localGitRepoService.saveLocalToDirectory(opensourceUri, localPath);
    GitTree treeOfRepo = localGitRepoService.getFlatTreeOfRepo(repo, repoReference);

    return opensourceRepoService.create(treeOfRepo);
  }

}
