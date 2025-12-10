package com.opensourcereader.core.analysis.exception.file;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.FILE_SYSTEM_DIRECTORY_CREATE_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalDirectoryCreationException extends OpenSourceRepoException {

  public LocalDirectoryCreationException() {
    super(FILE_SYSTEM_DIRECTORY_CREATE_FAILED);
  }
}
