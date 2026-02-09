package com.opensourcereader.core.analysis.exception.file;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.FILE_SYSTEM_DIRECTORY_DELETE_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalDirectoryDeletionException extends OpenSourceRepoException {

  public LocalDirectoryDeletionException() {
    super(FILE_SYSTEM_DIRECTORY_DELETE_FAILED);
  }
}
