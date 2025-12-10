package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_TREE_PARSE_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalGitTreeParseException extends OpenSourceRepoException {

  public LocalGitTreeParseException() {
    super(REPOSITORY_TREE_PARSE_FAILED);
  }
}
