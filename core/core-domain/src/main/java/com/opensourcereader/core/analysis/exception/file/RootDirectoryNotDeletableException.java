package com.opensourcereader.core.analysis.exception.file;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.FILE_SYSTEM_ROOT_DIRECTORY_NOT_DELETABLE;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class RootDirectoryNotDeletableException extends OpenSourceRepoException {

  public RootDirectoryNotDeletableException() {
    super(FILE_SYSTEM_ROOT_DIRECTORY_NOT_DELETABLE);
  }
}
