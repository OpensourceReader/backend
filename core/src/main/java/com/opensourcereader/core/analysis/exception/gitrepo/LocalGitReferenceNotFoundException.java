package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_REFERENCE_NOT_FOUND;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalGitReferenceNotFoundException extends OpenSourceRepoException {

  public LocalGitReferenceNotFoundException() {
    super(REPOSITORY_REFERENCE_NOT_FOUND);
  }
}
