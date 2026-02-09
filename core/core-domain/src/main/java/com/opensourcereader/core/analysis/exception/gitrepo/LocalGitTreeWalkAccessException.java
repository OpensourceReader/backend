package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_TREE_WALK_ACCESS_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalGitTreeWalkAccessException extends OpenSourceRepoException {

  public LocalGitTreeWalkAccessException() {
    super(REPOSITORY_TREE_WALK_ACCESS_FAILED);
  }
}
