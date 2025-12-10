package com.opensourcereader.core.analysis.exception.opensourcerepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_AlREADY_EXIST;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class OpenSourceRepoAlreadyExistException extends OpenSourceRepoException {

  public OpenSourceRepoAlreadyExistException() {
    super(REPOSITORY_AlREADY_EXIST);
  }
}
