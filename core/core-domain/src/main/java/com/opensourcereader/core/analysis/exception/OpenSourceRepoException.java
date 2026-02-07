package com.opensourcereader.core.analysis.exception;

import com.opensourcereader.core.shared.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class OpenSourceRepoException extends OpenSourceReaderException {

  public OpenSourceRepoException(OpenSourceRepoErrorCode openSourceRepoErrorCode) {
    super(openSourceRepoErrorCode);
  }
}
