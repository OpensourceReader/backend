package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.gitrepo.GitRepositoryLoadResult;
import com.opensourcereader.core.analysis.service.GitRepositoryLoader;
import com.opensourcereader.core.analysis.service.impl.gitrepo.EclipseJGitService;
import org.eclipse.jgit.lib.Repository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocalGitRepositoryLoader implements GitRepositoryLoader {

  private final EclipseJGitService eclipseJGitService;

  @Override
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
              return new OpenSourceFileInfo(fileInfo.path(), fileInfo.type(), rawText);
            })
        .toList();
  }
}
