package com.opensourcereader.api.facade.analysis;

import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpensourceRepo;
import com.opensourcereader.core.analysis.service.OpensourceRepoService;
import com.opensourcereader.core.analysis.service.local.GitRepoLocalManageService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpensourceRepoFacade {

  private final GitRepoLocalManageService gitRepoLocalManageService;
  private final OpensourceRepoService opensourceRepoService;

  public OpensourceRepo create(String opensourceUri, String localPath, String repoReference) {
    Repository repo = gitRepoLocalManageService.saveLocalToDirectory(opensourceUri, localPath);
    GitTree treeOfRepo = gitRepoLocalManageService.getFlatTreeOfRepo(repo, repoReference);

    return opensourceRepoService.create(treeOfRepo);
  }

}
