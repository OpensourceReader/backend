package com.opensourcereader.core.analysis.exception.gitrepo;

import static com.opensourcereader.core.analysis.exception.OpenSourceRepoErrorCode.REPOSITORY_CLONE_DIRECTORY_ALREADY_EXIST;

import com.opensourcereader.core.analysis.exception.OpenSourceRepoException;

public class LocalGitCloneDirectoryAlreadyExist extends OpenSourceRepoException {

  public LocalGitCloneDirectoryAlreadyExist() {
    super(REPOSITORY_CLONE_DIRECTORY_ALREADY_EXIST);
  }
}
