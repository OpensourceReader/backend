package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_OPEN_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalGitRepositoryOpenException extends OpenSourceRepoException {

  public LocalGitRepositoryOpenException() {
    super(REPOSITORY_OPEN_FAILED);
  }
}
