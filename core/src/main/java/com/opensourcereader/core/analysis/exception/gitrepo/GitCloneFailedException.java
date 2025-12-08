package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_CLONE_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class GitCloneFailedException extends OpenSourceRepoException {

  public GitCloneFailedException() {
    super(REPOSITORY_CLONE_FAILED);
  }
}
