package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_BLOB_LOAD_FAILED;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalGitBlobLoadException extends OpenSourceRepoException {

  public LocalGitBlobLoadException() {
    super(REPOSITORY_BLOB_LOAD_FAILED);
  }
}
