package com.opensourcereader.api.facade.analysis;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoResponse;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.gitrepo.GitRepositoryLoadResult;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.GitRepositoryLoader;
import com.opensourcereader.core.analysis.service.OpenSourceRepoClassMethodService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoMethodCallAnalyzer;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.analysis.util.FileUtil;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenSourceRepoFacade {

  @Value("${opensource-reader.local-clone-path}")
  private String localClonePath;

  @Value("${opensource-reader.work-tree-name}")
  private String workingTreeDirName;

  private final GitRepositoryLoader gitRepositoryLoader;
  private final OpenSourceRepoService opensourceRepoService;
  private final OpenSourceRepoMethodCallAnalyzer openSourceRepoMethodCallAnalyzer;
  private final OpenSourceRepoClassMethodService openSourceRepoClassMethodService;

  @Transactional
  public OpenSourceRepoResponse createRepo(OpenSourceRepoCreateRequest request) {
    GitRepositoryLoadResult gitRepoLoadResult =
        gitRepositoryLoader.downloadGitRepo(
            request.openSourceUri(), request.reference(), localClonePath);
    List<ClassStructure> classStructures =
        openSourceRepoMethodCallAnalyzer.createClassStructures(
            gitRepoLoadResult.savedLocalRepoPath(), request.reference(), workingTreeDirName);
    OpenSourceRepo openSourceRepo =
        opensourceRepoService.createRepo(
            request.openSourceUri(), gitRepoLoadResult.files(), classStructures);
    openSourceRepoClassMethodService.createMethodCallGraph(openSourceRepo.getId(), classStructures);
    FileUtil.removeDirectory(gitRepoLoadResult.savedLocalRepoPath());

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
