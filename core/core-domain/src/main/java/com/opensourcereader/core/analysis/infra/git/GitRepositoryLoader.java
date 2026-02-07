package com.opensourcereader.core.analysis.infra.git;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.infra.dto.gitrepo.GitRepositoryLoadResult;
import org.eclipse.jgit.lib.Repository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GitRepositoryLoader {

  private final EclipseJGitService eclipseJGitService;

  public GitRepositoryLoadResult downloadGitRepo(
      String openSourceUri, String reference, String localClonePath) {
    Path savedLocalRepoPath = eclipseJGitService.saveToLocal(openSourceUri, localClonePath);
    Repository repo = eclipseJGitService.createRepositoryBuilder(savedLocalRepoPath);

    return new GitRepositoryLoadResult(
        savedLocalRepoPath, getOpenSourceFileInfos(reference, savedLocalRepoPath, repo));
  }

  private List<OpenSourceFileInfo> getOpenSourceFileInfos(
      String reference, Path savedLocalPath, Repository repo) {
    return eclipseJGitService.getFlatTree(savedLocalPath, reference).stream()
        .map(
            fileInfo -> {
              String rawText = eclipseJGitService.getRawText(fileInfo.blobId(), repo);
              return OpenSourceFileInfo.of(fileInfo.path(), fileInfo.type(), rawText);
            })
        .toList();
  }
}
