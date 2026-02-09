package com.opensourcereader.core.analysis.exception.opensourcerepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_NOT_FOUND;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class OpenSourceRepoNotFoundException extends OpenSourceRepoException {

  public OpenSourceRepoNotFoundException() {
    super(REPOSITORY_NOT_FOUND);
  }
}
