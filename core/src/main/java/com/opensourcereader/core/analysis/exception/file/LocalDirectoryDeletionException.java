package com.opensourcereader.core.analysis.exception.file;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.LOCAL_DIRECTORY_DELETE_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalDirectoryDeletionException extends OpenSourceRepoException {

  public LocalDirectoryDeletionException() {
    super(LOCAL_DIRECTORY_DELETE_FAILED);
  }
}
