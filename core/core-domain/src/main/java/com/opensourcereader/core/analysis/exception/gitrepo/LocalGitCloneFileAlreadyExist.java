package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_CLONE_FILE_ALREADY_EXIST;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalGitCloneFileAlreadyExist extends OpenSourceRepoException {

  public LocalGitCloneFileAlreadyExist() {
    super(REPOSITORY_CLONE_FILE_ALREADY_EXIST);
  }
}
