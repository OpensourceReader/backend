package com.opensourcereader.core.analysis.exception.file;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.LOCAL_DIRECTORY_CREATE_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalDirectoryCreationException extends OpenSourceRepoException {

  public LocalDirectoryCreationException() {
    super(LOCAL_DIRECTORY_CREATE_FAILED);
  }
}
