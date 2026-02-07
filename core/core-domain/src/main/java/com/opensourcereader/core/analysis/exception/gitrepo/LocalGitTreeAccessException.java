package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_TREE_ACCESS_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalGitTreeAccessException extends OpenSourceRepoException {

  public LocalGitTreeAccessException() {
    super(REPOSITORY_TREE_ACCESS_FAILED);
  }
}
