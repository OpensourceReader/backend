package com.opensourcereader.core.analysis.exception;

import com.opensourcereader.core.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class OpenSourceRepoException extends OpenSourceReaderException {

  private final OpenSourceRepoErrorCode openSourceRepoErrorCode;

  public OpenSourceRepoException(OpenSourceRepoErrorCode openSourceRepoErrorCode) {
    this.openSourceRepoErrorCode = openSourceRepoErrorCode;
  }
}
