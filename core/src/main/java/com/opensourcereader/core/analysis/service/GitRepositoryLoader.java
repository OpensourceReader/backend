package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.dto.gitrepo.GitRepositoryLoadResult;

public interface GitRepositoryLoader {

  GitRepositoryLoadResult downloadGitRepo(
      String openSourceUri, String reference, String localClonePath);
}
